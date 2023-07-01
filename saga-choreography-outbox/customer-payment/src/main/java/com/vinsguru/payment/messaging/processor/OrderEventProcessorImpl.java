package com.vinsguru.payment.messaging.processor;

import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.events.payment.PaymentEvent;
import com.vinsguru.common.exception.EventAlreadyProcessedException;
import com.vinsguru.common.processor.OrderEventProcessor;
import com.vinsguru.payment.common.service.PaymentService;
import com.vinsguru.payment.messaging.mapper.MessageDtoMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.UnaryOperator;

@Service
@RequiredArgsConstructor
public class OrderEventProcessorImpl implements OrderEventProcessor<PaymentEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProcessorImpl.class);
    private final PaymentService service;

    @Override
    public Mono<PaymentEvent> handle(OrderEvent.OrderCreated event) {
        return this.service.process(MessageDtoMapper.toPaymentProcessRequest(event))
                           .map(MessageDtoMapper::toPaymentDeductedEvent)
                           .doOnNext(e -> log.info("payment processed {}", e))
                           .transform(exceptionHandler(event));
    }

    @Override
    public Mono<PaymentEvent> handle(OrderEvent.OrderCancelled event) {
        return this.service.refund(event.orderId())
                           .map(MessageDtoMapper::toPaymentRefundedEvent)
                           .doOnNext(e -> log.info("refund processed {}", e))
                           .doOnError(ex -> log.error("error while processing refund", ex));
    }

    @Override
    public Mono<PaymentEvent> handle(OrderEvent.OrderCompleted event) {
        return Mono.empty();
    }

    private UnaryOperator<Mono<PaymentEvent>> exceptionHandler(OrderEvent.OrderCreated event) {
        return mono -> mono.onErrorResume(EventAlreadyProcessedException.class, e -> Mono.empty())
                           .onErrorResume(MessageDtoMapper.toPaymentDeclinedEvent(event));
    }

}
