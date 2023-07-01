package com.vinsguru.common.messages.shipping;

import com.vinsguru.common.messages.Response;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

public sealed interface ShippingResponse extends Response {

    /*
        Intentionally named as Schedule / Scheduled as these are inner classes.
        Feel free to change if you do not like it
     */

    @Builder
    record Scheduled(UUID orderId,
                     UUID shipmentId,
                     Instant deliveryDate) implements ShippingResponse {

    }

    @Builder
    record Declined(UUID orderId,
                    String message) implements ShippingResponse {

    }

}
