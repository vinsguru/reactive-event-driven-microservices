package com.vinsguru.payment;

import com.vinsguru.common.events.order.OrderEvent;

import java.time.Instant;
import java.util.UUID;

public class TestDataUtil {

    public static OrderEvent.OrderCreated createOrderCreatedEvent(int customerId, int productId, int unitPrice, int quantity) {
        return OrderEvent.OrderCreated.builder()
                                      .orderId(UUID.randomUUID())
                                      .createdAt(Instant.now())
                                      .totalAmount(unitPrice * quantity)
                                      .unitPrice(unitPrice)
                                      .quantity(quantity)
                                      .customerId(customerId)
                                      .productId(productId)
                                      .build();
    }

    public static OrderEvent.OrderCancelled createOrderCancelledEvent(UUID orderId) {
        return OrderEvent.OrderCancelled.builder()
                                        .orderId(orderId)
                                        .createdAt(Instant.now())
                                        .build();
    }

}
