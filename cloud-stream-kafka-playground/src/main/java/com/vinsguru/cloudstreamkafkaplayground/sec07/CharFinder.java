package com.vinsguru.cloudstreamkafkaplayground.sec07;

import com.vinsguru.cloudstreamkafkaplayground.common.MessageConverter;
import com.vinsguru.cloudstreamkafkaplayground.common.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Configuration
public class CharFinder {

    private static final Logger log = LoggerFactory.getLogger(CharFinder.class);
    private static final String DLT_TOPIC = "dlt-topic";

    @Autowired
    private StreamBridge streamBridge;

    @Bean
    public Function<Flux<Message<String>>, Flux<Character>> processor() {
        return flux -> flux
                .map(MessageConverter::toRecord)
                .concatMap(r -> this.find(r.message())
                                    .onErrorResume(ex -> Mono.fromRunnable(() -> this.handleError(ex, r)))
                                    .doAfterTerminate(() -> r.acknowledgement().acknowledge())
                );
    }

    private Mono<Character> find(String message) {
        return Mono.just(message)
                   .map(m -> m.charAt(3));
    }

    private void handleError(Throwable ex, Record<String> record) {
        log.error(ex.getMessage());
        this.streamBridge.send(
                DLT_TOPIC,
                MessageBuilder.withPayload(record.message())
                              .setHeader(KafkaHeaders.KEY, record.key())
                              .build()
        );
    }

}
