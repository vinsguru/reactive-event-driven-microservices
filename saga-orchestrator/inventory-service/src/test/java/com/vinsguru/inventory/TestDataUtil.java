package com.vinsguru.inventory;

import com.vinsguru.common.messages.inventory.InventoryRequest;

import java.util.UUID;

public class TestDataUtil {

    public static InventoryRequest createDeductRequest(UUID orderId, int productId, int quantity) {
        return InventoryRequest.Deduct.builder()
                                      .orderId(orderId)
                                      .productId(productId)
                                      .quantity(quantity)
                                      .build();
    }

    public static InventoryRequest createRestoreRequest(UUID orderId) {
        return InventoryRequest.Restore.builder()
                                     .orderId(orderId)
                                     .build();
    }

}
