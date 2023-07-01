package com.vinsguru.inventory.messaging.mapper;

import com.vinsguru.common.messages.inventory.InventoryRequest;
import com.vinsguru.common.messages.inventory.InventoryResponse;
import com.vinsguru.inventory.common.dto.InventoryDeductRequest;
import com.vinsguru.inventory.common.dto.OrderInventoryDto;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class MessageDtoMapper {

    public static InventoryDeductRequest toInventoryDeductRequest(InventoryRequest.Deduct request) {
        return InventoryDeductRequest.builder()
                                     .orderId(request.orderId())
                                     .productId(request.productId())
                                     .quantity(request.quantity())
                                     .build();
    }

    public static InventoryResponse toInventoryDeductedResponse(OrderInventoryDto orderInventoryDto) {
        return InventoryResponse.Deducted.builder()
                                               .orderId(orderInventoryDto.orderId())
                                               .inventoryId(orderInventoryDto.inventoryId())
                                               .productId(orderInventoryDto.productId())
                                               .quantity(orderInventoryDto.quantity())
                                               .build();
    }

    public static Function<Throwable, Mono<InventoryResponse>> toInventoryDeclinedResponse(InventoryRequest.Deduct request) {
        return ex -> Mono.fromSupplier(() -> InventoryResponse.Declined.builder()
                                                                       .orderId(request.orderId())
                                                                       .message(ex.getMessage())
                                                                       .build()
        );
    }

}
