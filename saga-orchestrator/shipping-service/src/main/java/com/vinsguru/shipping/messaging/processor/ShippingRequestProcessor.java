package com.vinsguru.shipping.messaging.processor;

import com.vinsguru.common.messages.shipping.ShippingRequest;
import com.vinsguru.common.messages.shipping.ShippingResponse;
import com.vinsguru.common.processor.RequestProcessor;
import reactor.core.publisher.Mono;

public interface ShippingRequestProcessor extends RequestProcessor<ShippingRequest, ShippingResponse> {

    @Override
    default Mono<ShippingResponse> process(ShippingRequest request) {
        return switch (request){
            case ShippingRequest.Schedule s -> this.handle(s);
        };
    }

    Mono<ShippingResponse> handle(ShippingRequest.Schedule request);

}
