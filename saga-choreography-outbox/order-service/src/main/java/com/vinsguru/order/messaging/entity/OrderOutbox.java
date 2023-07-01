package com.vinsguru.order.messaging.entity;

import com.vinsguru.common.events.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderOutbox {

    @Id
    private Long id;
    private byte[] message;
    private OrderStatus status;
    private Instant createdAt;

}
