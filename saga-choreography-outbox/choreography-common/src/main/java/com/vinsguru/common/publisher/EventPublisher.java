package com.vinsguru.common.publisher;

import com.vinsguru.common.events.DomainEvent;
import com.vinsguru.common.outbox.Outbox;
import reactor.core.publisher.Flux;

public interface EventPublisher<T extends DomainEvent> {

    Flux<Outbox<T>> publish();

}
