package com.vinsguru.order.messaging.mapper;

import com.vinsguru.common.events.payment.PaymentEvent;
import com.vinsguru.common.events.payment.PaymentStatus;
import com.vinsguru.order.common.dto.OrderPaymentDto;

public class PaymentEventMapper {

    public static OrderPaymentDto toDto(PaymentEvent.PaymentDeducted event) {
        return OrderPaymentDto.builder()
                              .orderId(event.orderId())
                              .paymentId(event.paymentId())
                              .status(PaymentStatus.DEDUCTED)
                              .build();
    }

    public static OrderPaymentDto toDto(PaymentEvent.PaymentDeclined event) {
        return OrderPaymentDto.builder()
                              .orderId(event.orderId())
                              .status(PaymentStatus.DECLINED)
                              .message(event.message())
                              .build();
    }

    public static OrderPaymentDto toDto(PaymentEvent.PaymentRefunded event) {
        return OrderPaymentDto.builder()
                              .orderId(event.orderId())
                              .status(PaymentStatus.REFUNDED)
                              .build();
    }

}
