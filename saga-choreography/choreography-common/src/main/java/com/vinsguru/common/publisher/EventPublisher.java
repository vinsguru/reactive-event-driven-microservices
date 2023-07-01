package com.vinsguru.common.publisher;

import com.vinsguru.common.events.DomainEvent;
import reactor.core.publisher.Flux;

public interface EventPublisher<T extends DomainEvent> {

    Flux<T> publish();

}
