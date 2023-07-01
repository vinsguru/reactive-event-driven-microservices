package com.vinsguru.shipping.application.mapper;


import com.vinsguru.shipping.application.entity.Shipment;
import com.vinsguru.shipping.common.dto.ScheduleRequest;
import com.vinsguru.shipping.common.dto.ShipmentDto;

public class EntityDtoMapper {

    public static Shipment toShipment(ScheduleRequest request) {
        return Shipment.builder()
                       .customerId(request.customerId())
                       .orderId(request.orderId())
                       .productId(request.productId())
                       .quantity(request.quantity())
                       .build();
    }

    public static ShipmentDto toDto(Shipment shipment) {
        return ShipmentDto.builder()
                          .shipmentId(shipment.getId())
                          .customerId(shipment.getCustomerId())
                          .quantity(shipment.getQuantity())
                          .productId(shipment.getProductId())
                          .orderId(shipment.getOrderId())
                          .status(shipment.getStatus())
                          .deliveryDate(shipment.getDeliveryDate())
                          .build();
    }

}
