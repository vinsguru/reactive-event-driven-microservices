package com.vinsguru.common.events.inventory;

import com.vinsguru.common.events.DomainEvent;
import com.vinsguru.common.events.OrderSaga;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

public sealed interface InventoryEvent extends DomainEvent, OrderSaga {

    /*
       Intentionally using primitive wrapper types to keep things simple
    */

    @Builder
    record InventoryDeducted(UUID orderId,
                             UUID inventoryId,
                             Integer productId,
                             Integer quantity,
                             Instant createdAt) implements InventoryEvent {
    }

    @Builder
    record InventoryRestored(UUID orderId,
                             UUID inventoryId,
                             Integer productId,
                             Integer quantity,
                             Instant createdAt) implements InventoryEvent {
    }

    @Builder
    record InventoryDeclined(UUID orderId,
                             Integer productId,
                             Integer quantity,
                             String message,
                             Instant createdAt) implements InventoryEvent {
    }

}
