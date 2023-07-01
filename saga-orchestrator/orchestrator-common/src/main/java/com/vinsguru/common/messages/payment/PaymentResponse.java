package com.vinsguru.common.messages.payment;

import com.vinsguru.common.messages.Response;
import lombok.Builder;

import java.util.UUID;

public sealed interface PaymentResponse extends Response {

    /*
        Intentionally named as Process / Processed as these are inner classes.
        Feel free to change if you do not like it
     */

    @Builder
    record Processed(UUID orderId,
                     UUID paymentId,
                     Integer customerId,
                     Integer amount) implements PaymentResponse {

    }

    @Builder
    record Declined(UUID orderId,
                    String message) implements PaymentResponse {

    }

}
