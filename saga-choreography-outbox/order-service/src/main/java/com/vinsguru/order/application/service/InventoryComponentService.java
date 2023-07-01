package com.vinsguru.order.application.service;

import com.vinsguru.order.application.entity.OrderInventory;
import com.vinsguru.order.application.mapper.EntityDtoMapper;
import com.vinsguru.order.application.repository.OrderInventoryRepository;
import com.vinsguru.order.common.dto.OrderInventoryDto;
import com.vinsguru.order.common.service.OrderFulfillmentService;
import com.vinsguru.order.common.service.inventory.InventoryComponentFetcher;
import com.vinsguru.order.common.service.inventory.InventoryComponentStatusListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryComponentService implements InventoryComponentFetcher, InventoryComponentStatusListener {

    private static final OrderInventoryDto DEFAULT = OrderInventoryDto.builder().build();
    private final OrderInventoryRepository repository;
    private final OrderFulfillmentService fulfillmentService;

    @Override
    public Mono<OrderInventoryDto> getComponent(UUID orderId) {
        return this.repository.findByOrderId(orderId)
                              .map(EntityDtoMapper::toOrderInventoryDto)
                              .defaultIfEmpty(DEFAULT);
    }

    @Override
    public Mono<Void> onSuccess(OrderInventoryDto message) {
        return this.repository.findByOrderId(message.orderId())
                              .switchIfEmpty(Mono.defer(() -> this.add(message, true)))
                              .then(this.fulfillmentService.complete(message.orderId()));
    }

    @Override
    public Mono<Void> onFailure(OrderInventoryDto message) {
        return this.repository.findByOrderId(message.orderId())
                              .switchIfEmpty(Mono.defer(() -> this.add(message, false)))
                              .then(this.fulfillmentService.cancel(message.orderId()));
    }

    @Override
    public Mono<Void> onRollback(OrderInventoryDto message) {
        return this.repository.findByOrderId(message.orderId())
                              .doOnNext(e -> e.setStatus(message.status()))
                              .flatMap(this.repository::save)
                              .then();
    }

    private Mono<OrderInventory> add(OrderInventoryDto dto, boolean isSuccess) {
        var entity = EntityDtoMapper.toOrderInventory(dto);
        entity.setSuccess(isSuccess);
        return this.repository.save(entity);
    }

}
