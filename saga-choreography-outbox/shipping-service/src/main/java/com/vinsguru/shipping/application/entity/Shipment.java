package com.vinsguru.shipping.application.entity;

import com.vinsguru.common.events.shipping.ShippingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {

    @Id
    private UUID id;
    private UUID orderId;
    private Integer productId;
    private Integer customerId;
    private Integer quantity;
    private Instant deliveryDate;
    private ShippingStatus status;

}
