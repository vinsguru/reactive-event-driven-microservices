package com.vinsguru.common.processor;

import com.vinsguru.common.events.DomainEvent;
import com.vinsguru.common.events.payment.PaymentEvent;
import reactor.core.publisher.Mono;

public interface PaymentEventProcessor<R extends DomainEvent> extends EventProcessor<PaymentEvent, R> {

    @Override
    default Mono<R> process(PaymentEvent event) {
        return switch (event) {
            case PaymentEvent.PaymentDeducted e -> this.handle(e);
            case PaymentEvent.PaymentDeclined e -> this.handle(e);
            case PaymentEvent.PaymentRefunded e -> this.handle(e);
        };
    }

    Mono<R> handle(PaymentEvent.PaymentDeducted event);

    Mono<R> handle(PaymentEvent.PaymentDeclined event);

    Mono<R> handle(PaymentEvent.PaymentRefunded event);

}
