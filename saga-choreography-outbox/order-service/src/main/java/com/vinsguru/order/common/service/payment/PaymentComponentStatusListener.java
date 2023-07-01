package com.vinsguru.order.common.service.payment;

import com.vinsguru.order.common.dto.OrderPaymentDto;
import com.vinsguru.order.common.service.OrderComponentStatusListener;

public interface PaymentComponentStatusListener extends OrderComponentStatusListener<OrderPaymentDto> {
}
