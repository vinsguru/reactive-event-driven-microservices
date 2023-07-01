package com.vinsguru.common.processor;

import com.vinsguru.common.events.DomainEvent;
import reactor.core.publisher.Mono;

public interface EventProcessor<T extends DomainEvent, R extends DomainEvent> {

    Mono<R> process(T event);

}
