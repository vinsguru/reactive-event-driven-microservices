package com.vinsguru.order.messaging.mapper;


import com.vinsguru.common.events.inventory.InventoryEvent;
import com.vinsguru.common.events.inventory.InventoryStatus;
import com.vinsguru.order.common.dto.OrderInventoryDto;


public class InventoryEventMapper {

    public static OrderInventoryDto toDto(InventoryEvent.InventoryDeducted event) {
        return OrderInventoryDto.builder()
                                .orderId(event.orderId())
                                .inventoryId(event.inventoryId())
                                .status(InventoryStatus.DEDUCTED)
                                .build();
    }

    public static OrderInventoryDto toDto(InventoryEvent.InventoryDeclined event) {
        return OrderInventoryDto.builder()
                                .orderId(event.orderId())
                                .status(InventoryStatus.DECLINED)
                                .message(event.message())
                                .build();
    }

    public static OrderInventoryDto toDto(InventoryEvent.InventoryRestored event) {
        return OrderInventoryDto.builder()
                                .orderId(event.orderId())
                                .status(InventoryStatus.RESTORED)
                                .build();
    }

}
