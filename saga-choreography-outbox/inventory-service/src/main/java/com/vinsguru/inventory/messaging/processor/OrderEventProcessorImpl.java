package com.vinsguru.inventory.messaging.processor;

import com.vinsguru.common.events.inventory.InventoryEvent;
import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.exception.EventAlreadyProcessedException;
import com.vinsguru.common.processor.OrderEventProcessor;
import com.vinsguru.inventory.common.service.InventoryService;
import com.vinsguru.inventory.messaging.mapper.MessageDtoMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.UnaryOperator;

@Service
@RequiredArgsConstructor
public class OrderEventProcessorImpl implements OrderEventProcessor<InventoryEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProcessorImpl.class);
    private final InventoryService service;

    @Override
    public Mono<InventoryEvent> handle(OrderEvent.OrderCreated event) {
        return this.service.deduct(MessageDtoMapper.toInventoryDeductRequest(event))
                           .map(MessageDtoMapper::toInventoryDeductedEvent)
                           .doOnNext(e -> log.info("inventory deducted {}", e))
                           .transform(exceptionHandler(event));
    }

    @Override
    public Mono<InventoryEvent> handle(OrderEvent.OrderCancelled event) {
        return this.service.restore(event.orderId())
                           .map(MessageDtoMapper::toInventoryRestoredEvent)
                           .doOnNext(e -> log.info("inventory restored {}", e))
                           .doOnError(ex -> log.error("error while processing restore", ex));
    }

    @Override
    public Mono<InventoryEvent> handle(OrderEvent.OrderCompleted event) {
        return Mono.empty();
    }

    private UnaryOperator<Mono<InventoryEvent>> exceptionHandler(OrderEvent.OrderCreated event) {
        return mono -> mono.onErrorResume(EventAlreadyProcessedException.class, e -> Mono.empty())
                           .onErrorResume(MessageDtoMapper.toInventoryDeclinedEvent(event));
    }

}
