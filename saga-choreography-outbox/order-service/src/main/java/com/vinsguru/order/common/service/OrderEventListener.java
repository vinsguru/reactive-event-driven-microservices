package com.vinsguru.order.common.service;

import com.vinsguru.order.common.dto.PurchaseOrderDto;
import reactor.core.publisher.Mono;

public interface OrderEventListener {

    Mono<Void> onOrderCreated(PurchaseOrderDto dto);

    Mono<Void> onOrderCancelled(PurchaseOrderDto dto);

    Mono<Void> onOrderCompleted(PurchaseOrderDto dto);

}
