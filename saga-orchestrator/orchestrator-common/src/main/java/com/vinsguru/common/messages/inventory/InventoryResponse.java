package com.vinsguru.common.messages.inventory;

import com.vinsguru.common.messages.Response;
import lombok.Builder;

import java.util.UUID;

public sealed interface InventoryResponse extends Response {

    /*
        Intentionally named as Deduct / Deducted as these are inner classes.
        Feel free to change if you do not like it
     */

    @Builder
    record Deducted(UUID orderId,
                     UUID inventoryId,
                     Integer productId,
                     Integer quantity) implements InventoryResponse {

    }

    @Builder
    record Declined(UUID orderId,
                    String message) implements InventoryResponse {

    }

}
