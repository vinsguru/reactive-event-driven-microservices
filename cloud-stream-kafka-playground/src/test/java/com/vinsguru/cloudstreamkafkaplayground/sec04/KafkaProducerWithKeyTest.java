package com.vinsguru.cloudstreamkafkaplayground.sec04;

import com.vinsguru.cloudstreamkafkaplayground.AbstractIntegrationTest;
import com.vinsguru.cloudstreamkafkaplayground.common.MessageConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.function.Consumer;

@TestPropertySource(properties = {
        "sec=sec04",
        "spring.cloud.function.definition=producer;testConsumer",
        "spring.cloud.stream.bindings.testConsumer-in-0.destination=input-topic"
})
public class KafkaProducerWithKeyTest extends AbstractIntegrationTest {

    private static final Sinks.Many<Message<String>> sink = Sinks.many().unicast().onBackpressureBuffer();

    @Test
    public void producerTest(){
        sink.asFlux()
             .map(MessageConverter::toRecord)
            .take(1)
            .timeout(Duration.ofSeconds(5))
            .as(StepVerifier::create)
            .consumeNextWith(r -> {
                Assertions.assertEquals("msg 0", r.message());
                Assertions.assertEquals("key-0", r.key());
            })
            .verifyComplete();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public Consumer<Flux<Message<String>>> testConsumer(){
            return f -> f.doOnNext(sink::tryEmitNext).subscribe();
        }

    }

}
