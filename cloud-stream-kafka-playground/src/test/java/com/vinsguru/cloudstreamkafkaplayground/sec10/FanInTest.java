package com.vinsguru.cloudstreamkafkaplayground.sec10;

import com.vinsguru.cloudstreamkafkaplayground.AbstractIntegrationTest;
import com.vinsguru.cloudstreamkafkaplayground.sec05.dto.DigitalDelivery;
import com.vinsguru.cloudstreamkafkaplayground.sec05.dto.OrderEvent;
import com.vinsguru.cloudstreamkafkaplayground.sec05.dto.OrderType;
import com.vinsguru.cloudstreamkafkaplayground.sec05.dto.PhysicalDelivery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
        "sec=sec10",
        "spring.cloud.function.definition=processor;tempProducer;rhProducer;heatIndexConsumer",
        "spring.cloud.stream.bindings.tempProducer-out-0.destination=temperature-topic",
        "spring.cloud.stream.bindings.rhProducer-out-0.destination=humidity-topic",
        "spring.cloud.stream.bindings.processor-in-0.destination=temperature-topic",
        "spring.cloud.stream.bindings.processor-in-1.destination=humidity-topic",
        "spring.cloud.stream.bindings.processor-out-0.destination=heat-index-topic",
        "spring.cloud.stream.bindings.heatIndexConsumer-in-0.destination=heat-index-topic"
})
public class FanInTest extends AbstractIntegrationTest {

    private static final Sinks.Many<Integer> tempSink = Sinks.many().unicast().onBackpressureBuffer();
    private static final Sinks.Many<Integer> rhSink = Sinks.many().unicast().onBackpressureBuffer();
    private static final Sinks.Many<Long> heatIndexSink = Sinks.many().unicast().onBackpressureBuffer();

    @Test
    public void heatIndexTest(){

        heatIndexSink.asFlux()
                     .take(3)
                     .timeout(Duration.ofSeconds(5))
                     .as(StepVerifier::create)
                     .then(() -> tempSink.tryEmitNext(90))
                     .then(() -> rhSink.tryEmitNext(55))
                     .consumeNextWith(index -> Assertions.assertEquals(97, index))
                     .then(() -> rhSink.tryEmitNext(60))
                     .consumeNextWith(index -> Assertions.assertEquals(100, index))
                     .then(() -> tempSink.tryEmitNext(94))
                     .consumeNextWith(index -> Assertions.assertEquals(110, index))
                     .verifyComplete();

    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public Supplier<Flux<Integer>> tempProducer(){
            return tempSink::asFlux;
        }

        @Bean
        public Supplier<Flux<Integer>> rhProducer(){
            return rhSink::asFlux;
        }

        @Bean
        public Consumer<Flux<Long>> heatIndexConsumer(){
            return f -> f.doOnNext(heatIndexSink::tryEmitNext).subscribe();
        }

    }

}
