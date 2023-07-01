package com.vinsguru.order.messaging.mapper;

import com.vinsguru.common.messages.inventory.InventoryRequest;
import com.vinsguru.common.messages.payment.PaymentRequest;
import com.vinsguru.common.messages.shipping.ShippingRequest;
import com.vinsguru.common.messages.shipping.ShippingResponse;
import com.vinsguru.order.common.dto.OrderShipmentSchedule;
import com.vinsguru.order.common.dto.PurchaseOrderDto;

import java.util.UUID;

public class MessageDtoMapper {

    public static PaymentRequest toPaymentProcessRequest(PurchaseOrderDto dto) {
        return PaymentRequest.Process.builder()
                                     .orderId(dto.orderId())
                                     .amount(dto.amount())
                                     .customerId(dto.customerId())
                                     .build();
    }

    public static PaymentRequest toPaymentRefundRequest(UUID orderId) {
        return PaymentRequest.Refund.builder()
                                    .orderId(orderId)
                                    .build();
    }

    public static InventoryRequest toInventoryDeductRequest(PurchaseOrderDto dto) {
        return InventoryRequest.Deduct.builder()
                                      .orderId(dto.orderId())
                                      .productId(dto.productId())
                                      .quantity(dto.quantity())
                                      .build();
    }

    public static InventoryRequest toInventoryRestoreRequest(UUID orderId) {
        return InventoryRequest.Restore.builder()
                                       .orderId(orderId)
                                       .build();
    }

    public static ShippingRequest toShippingScheduleRequest(PurchaseOrderDto dto) {
        return ShippingRequest.Schedule.builder()
                                       .orderId(dto.orderId())
                                       .customerId(dto.customerId())
                                       .quantity(dto.quantity())
                                       .productId(dto.productId())
                                       .build();
    }

    public static OrderShipmentSchedule toShipmentSchedule(ShippingResponse.Scheduled response) {
        return OrderShipmentSchedule.builder()
                                    .orderId(response.orderId())
                                    .deliveryDate(response.deliveryDate())
                                    .build();
    }

}
