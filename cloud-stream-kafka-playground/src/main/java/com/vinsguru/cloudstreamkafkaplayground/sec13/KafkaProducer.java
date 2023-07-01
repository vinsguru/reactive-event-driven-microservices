package com.vinsguru.cloudstreamkafkaplayground.sec13;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.function.Supplier;

/*
    goal: to demo sender results
 */

@Configuration
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    @Bean
    public FluxMessageChannel senderResults(){
        return new FluxMessageChannel();
    }

    @Bean
    public Supplier<Flux<Message<String>>> producer() {
        return () -> Flux.interval(Duration.ofSeconds(1))
                         .take(5)
                         .map(this::toMessage)
                         .doOnNext(m -> log.info("produced {}", m));
    }

    private Message<String> toMessage(long i) {
        return MessageBuilder.withPayload("msg " + i)
                             .setHeader(KafkaHeaders.KEY, ("key-" + i))
                             .setHeader(IntegrationMessageHeaderAccessor.CORRELATION_ID, i)
                             .build();
    }

}
