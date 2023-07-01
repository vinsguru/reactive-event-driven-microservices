package com.vinsguru.order.application.entity;

import com.vinsguru.common.events.payment.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPayment {

    /*
        We intentionally maintain 1:1 relationship
     */

    @Id
    private Integer id;
    private UUID orderId;
    private UUID paymentId;
    private PaymentStatus status;
    private Boolean success;
    private String message;

}
