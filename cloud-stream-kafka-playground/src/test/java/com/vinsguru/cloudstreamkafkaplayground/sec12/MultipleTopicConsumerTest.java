package com.vinsguru.cloudstreamkafkaplayground.sec12;

import com.vinsguru.cloudstreamkafkaplayground.AbstractIntegrationTest;
import com.vinsguru.cloudstreamkafkaplayground.sec11.dto.ContactMethod;
import com.vinsguru.cloudstreamkafkaplayground.sec11.dto.Email;
import com.vinsguru.cloudstreamkafkaplayground.sec11.dto.Phone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

@TestPropertySource(properties = {
        "sec=sec12",
        "spring.cloud.function.definition=consumer;producer1;producer2",
        "spring.cloud.stream.bindings.producer1-out-0.destination=input-topic1",
        "spring.cloud.stream.bindings.producer2-out-0.destination=input-topic2"
})
@ExtendWith(OutputCaptureExtension.class)
public class MultipleTopicConsumerTest extends AbstractIntegrationTest {

    private static final Sinks.Many<String> sink1 = Sinks.many().unicast().onBackpressureBuffer();
    private static final Sinks.Many<String> sink2 = Sinks.many().unicast().onBackpressureBuffer();

    @Test
    public void multipleTopicsTest(CapturedOutput output) throws InterruptedException {
        Thread.sleep(1000);
        sink1.tryEmitNext("msg1");
        sink2.tryEmitNext("msg2");
        sink1.tryEmitNext("msg3");
        Thread.sleep(1000);
        Assertions.assertTrue(output.getOut().contains("msg1"));
        Assertions.assertTrue(output.getOut().contains("msg2"));
        Assertions.assertTrue(output.getOut().contains("msg3"));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public Supplier<Flux<String>> producer1(){
            return sink1::asFlux;
        }

        @Bean
        public Supplier<Flux<String>> producer2(){
            return sink2::asFlux;
        }

    }
}
