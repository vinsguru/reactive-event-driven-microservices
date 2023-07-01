package com.vinsguru.order.messaging.publisher;

import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.publisher.EventPublisher;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderEventOutboxService extends EventPublisher<OrderEvent> {

    Mono<Void> deleteEvents(List<Long> ids);

}
