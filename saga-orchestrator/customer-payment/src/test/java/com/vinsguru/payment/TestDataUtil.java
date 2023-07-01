package com.vinsguru.payment;

import com.vinsguru.common.messages.payment.PaymentRequest;

import java.util.UUID;

public class TestDataUtil {

    public static PaymentRequest createProcessRequest(UUID orderId, int customerId, int amount) {
        return PaymentRequest.Process.builder()
                                      .orderId(orderId)
                                      .customerId(customerId)
                                      .amount(amount)
                                      .build();
    }

    public static PaymentRequest createRefundRequest(UUID orderId) {
        return PaymentRequest.Refund.builder()
                                     .orderId(orderId)
                                     .build();
    }

}
