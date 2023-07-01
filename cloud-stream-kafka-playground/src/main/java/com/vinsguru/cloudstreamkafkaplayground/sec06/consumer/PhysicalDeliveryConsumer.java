package com.vinsguru.cloudstreamkafkaplayground.sec06.consumer;

import com.vinsguru.cloudstreamkafkaplayground.common.MessageConverter;
import com.vinsguru.cloudstreamkafkaplayground.common.Record;
import com.vinsguru.cloudstreamkafkaplayground.sec06.dto.PhysicalDelivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Configuration
public class PhysicalDeliveryConsumer {

    private static final Logger log = LoggerFactory.getLogger(PhysicalDeliveryConsumer.class);

    @Bean
    public Function<Flux<Message<PhysicalDelivery>>, Mono<Void>> physicalDelivery() {
        return flux -> flux
                .map(MessageConverter::toRecord)
                .doOnNext(this::printDetails)
                .then();
    }

    private void printDetails(Record<PhysicalDelivery> record) {
        log.info("physical consumer {}", record.message());
        record.acknowledgement().acknowledge();
    }

}
