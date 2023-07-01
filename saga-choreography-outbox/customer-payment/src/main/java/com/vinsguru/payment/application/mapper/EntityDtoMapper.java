package com.vinsguru.payment.application.mapper;

import com.vinsguru.payment.application.entity.CustomerPayment;
import com.vinsguru.payment.common.dto.PaymentDto;
import com.vinsguru.payment.common.dto.PaymentProcessRequest;

public class EntityDtoMapper {

    public static CustomerPayment toCustomerPayment(PaymentProcessRequest request) {
        return CustomerPayment.builder()
                              .customerId(request.customerId())
                              .orderId(request.orderId())
                              .amount(request.amount())
                              .build();
    }

    public static PaymentDto toDto(CustomerPayment payment) {
        return PaymentDto.builder()
                         .amount(payment.getAmount())
                         .status(payment.getStatus())
                         .paymentId(payment.getPaymentId())
                         .customerId(payment.getCustomerId())
                         .orderId(payment.getOrderId())
                         .build();
    }

}
