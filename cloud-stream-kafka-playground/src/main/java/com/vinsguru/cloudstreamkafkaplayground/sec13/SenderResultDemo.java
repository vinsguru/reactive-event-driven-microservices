package com.vinsguru.cloudstreamkafkaplayground.sec13;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.SenderResult;

@Service
public class SenderResultDemo {

    private static final Logger log = LoggerFactory.getLogger(SenderResultDemo.class);

    @Autowired
    private FluxMessageChannel channel;

    @PostConstruct
    private void init(){

        Flux.from(channel)
                .map(Message::getPayload)
                .cast(SenderResult.class)
                .doOnNext(r -> log.info("received result id {}, record metadata {}", r.correlationMetadata(), r.recordMetadata()))
                .subscribe();


    }


}
