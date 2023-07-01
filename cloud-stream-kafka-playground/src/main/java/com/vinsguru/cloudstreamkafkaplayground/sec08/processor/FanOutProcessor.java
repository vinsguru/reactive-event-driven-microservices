package com.vinsguru.cloudstreamkafkaplayground.sec08.processor;

import com.vinsguru.cloudstreamkafkaplayground.common.MessageConverter;
import com.vinsguru.cloudstreamkafkaplayground.sec08.dto.DigitalDelivery;
import com.vinsguru.cloudstreamkafkaplayground.sec08.dto.OrderEvent;
import com.vinsguru.cloudstreamkafkaplayground.sec08.dto.PhysicalDelivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
public class FanOutProcessor {

    private static final Logger log = LoggerFactory.getLogger(FanOutProcessor.class);
    //    private static final String DIGITAL_DELIVERY_CHANNEL = "digital-delivery-out";
//    private static final String PHYSICAL_DELIVERY_CHANNEL = "physical-delivery-out";
    private static final String DIGITAL_DELIVERY_CHANNEL = "digital-topic";
    private static final String PHYSICAL_DELIVERY_CHANNEL = "physical-topic";
    private final Consumer<OrderEvent> digitalSend = this::toDigitalDelivery;
    private final Consumer<OrderEvent> physicalSend = this::toPhysicalDelivery;
    private final Consumer<OrderEvent> fanOut = digitalSend.andThen(physicalSend);

    @Autowired
    private StreamBridge streamBridge;

    @Bean
    public Function<Flux<Message<OrderEvent>>, Mono<Void>> processor() {
        return flux -> flux
                .map(MessageConverter::toRecord)
                .doOnNext(r -> this.route(r.message()))
                .doOnNext(r -> r.acknowledgement().acknowledge())
                .then();
    }

    private void route(OrderEvent event) {
        switch (event.orderType()) {
            case DIGITAL -> digitalSend.accept(event);
            case PHYSICAL -> fanOut.accept(event);
        }
    }

    private void toDigitalDelivery(OrderEvent event) {
        var dd = new DigitalDelivery(event.productId(), "%s@gmail.com".formatted(event.customerId()));
        this.streamBridge.send(DIGITAL_DELIVERY_CHANNEL, dd);
    }

    private void toPhysicalDelivery(OrderEvent event) {
        var pd = new PhysicalDelivery(
                event.productId(),
                "%s street".formatted(event.customerId()),
                "%s city".formatted(event.customerId()),
                "some country"
        );
        this.streamBridge.send(PHYSICAL_DELIVERY_CHANNEL, pd);
    }

}
