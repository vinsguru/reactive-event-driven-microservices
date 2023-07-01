package com.vinsguru.order.application.entity;

import com.vinsguru.common.events.inventory.InventoryStatus;
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

    /*
        We intentionally maintain 1:1 relationship
     */

    @Id
    private Integer id;
    private UUID orderId;
    private UUID inventoryId;
    private InventoryStatus status;
    private String message;
    private Boolean success;

}
