package com.vinsguru.cloudstreamkafkaplayground.sec03;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/*
    goal: to demo a simple processor to consume and do some processing and produce message
 */

@Configuration
public class KafkaProcessor {

    private static final Logger log = LoggerFactory.getLogger(KafkaProcessor.class);

    @Bean
    public Function<Flux<String>, Flux<String>> processor() {
        return flux -> flux
                .doOnNext(m -> log.info("processor received {}", m))
                .concatMap(this::process)
                .doOnNext(m -> log.info("after processing {}", m));
    }

    // service layer
    private Mono<String> process(String input) {
        return Mono.just(input) // could be a DB call etc
                   .map(String::toUpperCase);
    }

}
