package com.vinsguru.common.listener;

import com.vinsguru.common.events.shipping.ShippingEvent;
import reactor.core.publisher.Mono;

public interface ShippingEventListener extends EventListener<ShippingEvent> {

    /*
        To follow the same pattern as other event processors.
        also for type!
     */

    @Override
    default Mono<Void> listen(ShippingEvent event) {
        return switch (event) {
            case ShippingEvent.ShippingScheduled e -> this.handle(e);
        };
    }

    Mono<Void> handle(ShippingEvent.ShippingScheduled event);

}
