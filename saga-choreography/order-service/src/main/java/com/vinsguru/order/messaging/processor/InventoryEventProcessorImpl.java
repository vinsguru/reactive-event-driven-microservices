package com.vinsguru.order.messaging.processor;

import com.vinsguru.common.events.inventory.InventoryEvent;
import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.processor.InventoryEventProcessor;
import com.vinsguru.order.application.mapper.EntityDtoMapper;
import com.vinsguru.order.common.service.OrderFulfillmentService;
import com.vinsguru.order.common.service.inventory.InventoryComponentStatusListener;
import com.vinsguru.order.messaging.mapper.InventoryEventMapper;
import com.vinsguru.order.messaging.mapper.OrderEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class InventoryEventProcessorImpl implements InventoryEventProcessor<OrderEvent> {

    private final OrderFulfillmentService fulfillmentService;
    private final InventoryComponentStatusListener statusListener;

    @Override
    public Mono<OrderEvent> handle(InventoryEvent.InventoryDeducted event) {
        var dto = InventoryEventMapper.toDto(event);
        return this.statusListener.onSuccess(dto)
                                  .then(this.fulfillmentService.complete(event.orderId()))
                                  .map(OrderEventMapper::toOrderCompletedEvent);
    }

    @Override
    public Mono<OrderEvent> handle(InventoryEvent.InventoryDeclined event) {
        var dto = InventoryEventMapper.toDto(event);
        return this.statusListener.onFailure(dto)
                                  .then(this.fulfillmentService.cancel(event.orderId()))
                                  .map(OrderEventMapper::toOrderCancelledEvent);
    }

    @Override
    public Mono<OrderEvent> handle(InventoryEvent.InventoryRestored event) {
        var dto = InventoryEventMapper.toDto(event);
        return this.statusListener.onRollback(dto)
                                  .then(Mono.empty());
    }

}
