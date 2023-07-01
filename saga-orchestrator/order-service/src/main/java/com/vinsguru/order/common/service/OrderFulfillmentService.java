package com.vinsguru.order.common.service;

import com.vinsguru.order.common.dto.OrderShipmentSchedule;
import com.vinsguru.order.common.dto.PurchaseOrderDto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderFulfillmentService {

    Mono<PurchaseOrderDto> get(UUID orderId);

    Mono<PurchaseOrderDto> schedule(OrderShipmentSchedule shipmentSchedule);

    Mono<PurchaseOrderDto> complete(UUID orderId);

    Mono<PurchaseOrderDto> cancel(UUID orderId);

}
