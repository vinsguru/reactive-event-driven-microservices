package com.vinsguru.inventory.application.entity;

import com.vinsguru.common.messages.inventory.InventoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderInventory {

    @Id
    private UUID inventoryId;
    private UUID orderId;
    private Integer productId;
    private Integer quantity;
    private InventoryStatus status;

}
