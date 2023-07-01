package com.vinsguru.order;

import com.vinsguru.order.common.dto.OrderCreateRequest;

public class TestDataUtil {

    public static OrderCreateRequest toRequest(int customerId, int productId, int unitPrice, int quantity) {
        return OrderCreateRequest.builder()
                                 .unitPrice(unitPrice)
                                 .quantity(quantity)
                                 .customerId(customerId)
                                 .productId(productId)
                                 .build();
    }

}
