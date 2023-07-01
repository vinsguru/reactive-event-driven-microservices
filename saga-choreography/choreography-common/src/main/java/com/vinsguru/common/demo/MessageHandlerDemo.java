package com.vinsguru.common.demo;

import com.vinsguru.common.events.order.OrderEvent;

import java.util.UUID;

public class MessageHandlerDemo {


    public static void main(String[] args) {

        var event = OrderEvent.OrderCancelled.builder().orderId(UUID.randomUUID()).build();

        var result = MessageHandler.<OrderEvent, String>create(event)
                                   .onMessage(OrderEvent.OrderCreated.class, e -> "created")
                                   .onMessage(OrderEvent.OrderCancelled.class, e -> "cancelled")
                                   .onMessage(OrderEvent.OrderCompleted.class, e -> "completed")
                                   .handle();

        System.out.println(result);

    }

}
