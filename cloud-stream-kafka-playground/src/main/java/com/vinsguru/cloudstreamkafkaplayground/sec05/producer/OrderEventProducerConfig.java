package com.vinsguru.cloudstreamkafkaplayground.sec05.producer;

import com.vinsguru.cloudstreamkafkaplayground.sec05.dto.OrderEvent;
import com.vinsguru.cloudstreamkafkaplayground.sec05.dto.OrderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.function.Supplier;

@Configuration
public class OrderEventProducerConfig {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducerConfig.class);

    @Bean
    public Supplier<Flux<Message<OrderEvent>>> orderEventProducer() {
        return () -> Flux.range(1, 10)
                         .delayElements(Duration.ofSeconds(1))
                         .map(this::toMessage)
                         .doOnNext(m -> log.info("produced {}", m));
    }

    private Message<OrderEvent> toMessage(int i) {
        var type = i % 2 == 0 ? OrderType.DIGITAL : OrderType.PHYSICAL;
        var event = new OrderEvent(i, i, type);
        return MessageBuilder.withPayload(event)
                             .setHeader(KafkaHeaders.KEY, "order-id-" + i)
                             .build();
    }

}
