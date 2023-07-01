package com.vinsguru.order.messaging.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.events.order.OrderStatus;
import com.vinsguru.common.outbox.Outbox;
import com.vinsguru.order.messaging.entity.OrderOutbox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OutboxMapper {

    private final ObjectMapper mapper;

    public OrderOutbox toEntity(OrderEvent event, OrderStatus status) {
        return OrderOutbox.builder()
                          .status(status)
                          .createdAt(Instant.now())
                          .message(this.toBytes(event))
                          .build();
    }

    public Outbox<OrderEvent> toOutboxEvent(OrderOutbox outbox) {
        return Outbox.<OrderEvent>builder()
                     .correlationId(outbox.getId())
                     .event(this.toEvent(outbox.getStatus(), outbox.getMessage()))
                     .build();
    }

    private OrderEvent toEvent(OrderStatus status, byte[] bytes) {
        return switch (status) {
            case PENDING -> this.toObject(bytes, OrderEvent.OrderCreated.class);
            case CANCELLED -> this.toObject(bytes, OrderEvent.OrderCancelled.class);
            case COMPLETED -> this.toObject(bytes, OrderEvent.OrderCompleted.class);
        };
    }

    private byte[] toBytes(Object o) {
        try {
            return this.mapper.writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T toObject(byte[] bytes, Class<T> type) {
        try {
            return this.mapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
