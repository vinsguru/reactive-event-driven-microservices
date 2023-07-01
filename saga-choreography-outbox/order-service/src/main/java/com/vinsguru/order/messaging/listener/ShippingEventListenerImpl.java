package com.vinsguru.order.messaging.listener;

import com.vinsguru.common.events.shipping.ShippingEvent;
import com.vinsguru.common.listener.ShippingEventListener;
import com.vinsguru.order.common.service.shipping.ShippingComponentStatusListener;
import com.vinsguru.order.messaging.mapper.ShippingEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ShippingEventListenerImpl implements ShippingEventListener {

    private final ShippingComponentStatusListener statusListener;

    @Override
    public Mono<Void> handle(ShippingEvent.ShippingScheduled event) {
        var dto = ShippingEventMapper.toDto(event);
        return this.statusListener.onSuccess(dto);
    }

}
