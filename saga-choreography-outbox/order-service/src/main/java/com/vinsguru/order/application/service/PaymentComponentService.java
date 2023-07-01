package com.vinsguru.order.application.service;

import com.vinsguru.order.application.entity.OrderPayment;
import com.vinsguru.order.application.mapper.EntityDtoMapper;
import com.vinsguru.order.application.repository.OrderPaymentRepository;
import com.vinsguru.order.common.dto.OrderPaymentDto;
import com.vinsguru.order.common.service.OrderFulfillmentService;
import com.vinsguru.order.common.service.payment.PaymentComponentFetcher;
import com.vinsguru.order.common.service.payment.PaymentComponentStatusListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentComponentService implements PaymentComponentFetcher, PaymentComponentStatusListener {

    private static final OrderPaymentDto DEFAULT = OrderPaymentDto.builder().build();
    private final OrderPaymentRepository repository;
    private final OrderFulfillmentService fulfillmentService;

    @Override
    public Mono<OrderPaymentDto> getComponent(UUID orderId) {
        return this.repository.findByOrderId(orderId)
                              .map(EntityDtoMapper::toOrderPaymentDto)
                              .defaultIfEmpty(DEFAULT);
    }

    @Override
    public Mono<Void> onSuccess(OrderPaymentDto message) {
        return this.repository.findByOrderId(message.orderId())
                              .switchIfEmpty(Mono.defer(() -> this.add(message, true)))
                              .then(this.fulfillmentService.complete(message.orderId()));
    }

    @Override
    public Mono<Void> onFailure(OrderPaymentDto message) {
        return this.repository.findByOrderId(message.orderId())
                              .switchIfEmpty(Mono.defer(() -> this.add(message, false)))
                              .then(this.fulfillmentService.cancel(message.orderId()));
    }

    @Override
    public Mono<Void> onRollback(OrderPaymentDto message) {
        return this.repository.findByOrderId(message.orderId())
                              .doOnNext(e -> e.setStatus(message.status()))
                              .flatMap(this.repository::save)
                              .then();
    }

    private Mono<OrderPayment> add(OrderPaymentDto dto, boolean isSuccess) {
        var entity = EntityDtoMapper.toOrderPayment(dto);
        entity.setSuccess(isSuccess);
        return this.repository.save(entity);
    }

}
