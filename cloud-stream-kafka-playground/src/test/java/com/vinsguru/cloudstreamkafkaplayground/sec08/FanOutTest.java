package com.vinsguru.cloudstreamkafkaplayground.sec08;

import com.vinsguru.cloudstreamkafkaplayground.AbstractIntegrationTest;
import com.vinsguru.cloudstreamkafkaplayground.sec05.dto.DigitalDelivery;
import com.vinsguru.cloudstreamkafkaplayground.sec05.dto.OrderEvent;
import com.vinsguru.cloudstreamkafkaplayground.sec05.dto.OrderType;
import com.vinsguru.cloudstreamkafkaplayground.sec05.dto.PhysicalDelivery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

@TestPropertySource(properties = {
        "sec=sec08",
        "spring.cloud.function.definition=processor;testProducer;ddConsumer;pdConsumer",
        "spring.cloud.stream.bindings.testProducer-out-0.destination=order-events",
        "spring.cloud.stream.bindings.ddConsumer-in-0.destination=digital-topic",
        "spring.cloud.stream.bindings.pdConsumer-in-0.destination=physical-topic"
})
public class FanOutTest extends AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(FanOutTest.class);
    private static final Sinks.Many<OrderEvent> orderSink = Sinks.many().unicast().onBackpressureBuffer();
    private static final Sinks.Many<DigitalDelivery> ddSink = Sinks.many().unicast().onBackpressureBuffer();
    private static final Sinks.Many<PhysicalDelivery> pdSink = Sinks.many().unicast().onBackpressureBuffer();

    @Test
    public void fanOutTest(){

        // produce
        orderSink.tryEmitNext(new OrderEvent(1, 1, OrderType.DIGITAL));
        orderSink.tryEmitNext(new OrderEvent(2, 2, OrderType.PHYSICAL));
        orderSink.tryEmitNext(new OrderEvent(3, 3, OrderType.DIGITAL));

        // consume
        ddSink.asFlux()
              .doOnNext(r -> log.info("digital consumer received {}", r))
              .take(Duration.ofSeconds(1))
              .as(StepVerifier::create)
              .consumeNextWith(e -> Assertions.assertEquals("1@gmail.com", e.email()))
              .consumeNextWith(e -> Assertions.assertEquals("2@gmail.com", e.email()))
              .consumeNextWith(e -> Assertions.assertEquals("3@gmail.com", e.email()))
              .verifyComplete();

        pdSink.asFlux()
              .doOnNext(r -> log.info("physical consumer received {}", r))
              .take(Duration.ofSeconds(1))
              .as(StepVerifier::create)
              .consumeNextWith(e -> Assertions.assertEquals(2, e.productId()))
              .verifyComplete();

    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public Supplier<Flux<OrderEvent>> testProducer(){
            return orderSink::asFlux;
        }

        @Bean
        public Consumer<Flux<DigitalDelivery>> ddConsumer(){
            return f -> f.doOnNext(ddSink::tryEmitNext).subscribe();
        }

        @Bean
        public Consumer<Flux<PhysicalDelivery>> pdConsumer(){
            return f -> f.doOnNext(pdSink::tryEmitNext).subscribe();
        }

    }
}
