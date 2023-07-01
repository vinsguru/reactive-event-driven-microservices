package com.vinsguru.order.common.service;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderComponentFetcher<T> {

    Mono<T> getComponent(UUID orderId);

}
