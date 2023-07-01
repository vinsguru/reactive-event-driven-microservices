package com.vinsguru.common.listener;

import com.vinsguru.common.events.DomainEvent;
import reactor.core.publisher.Mono;

public interface EventListener<T extends DomainEvent> {

    Mono<Void> listen(T event);

}
