package com.vinsguru.order.common.service;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderFulfillmentService {

    Mono<Void> complete(UUID orderId);

    Mono<Void> cancel(UUID orderId);

}
