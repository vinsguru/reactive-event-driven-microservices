package com.vinsguru.inventory.common.service;

import com.vinsguru.inventory.common.dto.InventoryDeductRequest;
import com.vinsguru.inventory.common.dto.OrderInventoryDto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface InventoryService {

    Mono<OrderInventoryDto> deduct(InventoryDeductRequest request);

    Mono<OrderInventoryDto> restore(UUID orderId);

}
