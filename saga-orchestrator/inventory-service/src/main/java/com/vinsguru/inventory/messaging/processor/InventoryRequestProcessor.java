package com.vinsguru.inventory.messaging.processor;

import com.vinsguru.common.messages.inventory.InventoryRequest;
import com.vinsguru.common.messages.inventory.InventoryResponse;
import com.vinsguru.common.processor.RequestProcessor;
import reactor.core.publisher.Mono;

public interface InventoryRequestProcessor extends RequestProcessor<InventoryRequest, InventoryResponse> {

    @Override
    default Mono<InventoryResponse> process(InventoryRequest request) {
        return switch (request){
            case InventoryRequest.Deduct r -> this.handle(r);
            case InventoryRequest.Restore r -> this.handle(r);
        };
    }

    Mono<InventoryResponse> handle(InventoryRequest.Deduct request);

    Mono<InventoryResponse> handle(InventoryRequest.Restore request);

}
