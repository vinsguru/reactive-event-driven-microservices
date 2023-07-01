package com.vinsguru.cloudstreamkafkaplayground.sec03;

import com.vinsguru.cloudstreamkafkaplayground.AbstractIntegrationTest;
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
        "sec=sec03",
        "spring.cloud.function.definition=processor;testConsumer;testProducer",
        "spring.cloud.stream.bindings.testConsumer-in-0.destination=output-topic",
        "spring.cloud.stream.bindings.testProducer-out-0.destination=input-topic",
})
public class KafkaProcessorTest extends AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(KafkaProcessorTest.class);
    private static final Sinks.Many<String> reqSink = Sinks.many().unicast().onBackpressureBuffer();
    private static final Sinks.Many<String> resSink = Sinks.many().unicast().onBackpressureBuffer();

    @Test
    public void processorTest(){
        // produce
        reqSink.tryEmitNext("sam");
        reqSink.tryEmitNext("mike");

        // consume
        resSink.asFlux()
               .take(2)
               .timeout(Duration.ofSeconds(5))
               .doOnNext(m -> log.info("test received {}", m))
               .as(StepVerifier::create)
               .consumeNextWith(m -> Assertions.assertEquals("SAM", m))
               .consumeNextWith(m -> Assertions.assertEquals("MIKE", m))
               .verifyComplete();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public Supplier<Flux<String>> testProducer(){
            return reqSink::asFlux;
        }

        @Bean
        public Consumer<Flux<String>> testConsumer(){
            return f -> f.doOnNext(resSink::tryEmitNext).subscribe();
        }

    }

}
