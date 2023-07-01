package com.vinsguru.cloudstreamkafkaplayground.sec08.dto;

public record OrderEvent(int customerId,
                         int productId,
                         OrderType orderType) {
}
