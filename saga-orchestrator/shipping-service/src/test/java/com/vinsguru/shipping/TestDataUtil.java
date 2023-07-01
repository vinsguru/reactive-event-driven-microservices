package com.vinsguru.shipping;

import com.vinsguru.common.messages.shipping.ShippingRequest;

import java.util.UUID;

public class TestDataUtil {

    public static ShippingRequest.Schedule createScheduleRequest(UUID orderId, int customerId, int productId, int quantity) {
        return ShippingRequest.Schedule.builder()
                                       .orderId(orderId)
                                       .customerId(customerId)
                                       .productId(productId)
                                       .quantity(quantity)
                                       .build();
    }

}
