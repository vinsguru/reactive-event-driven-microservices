package com.vinsguru.cloudstreamkafkaplayground.sec01;

/*
    goal: to demo a simple kafka consumer using java functional interfaces
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

//    @Bean
//    public ReceiverOptionsCustomizer customizer(){
//        return (s, ro) -> {
//            log.info("******************************** {}", s);
//            return ro.consumerProperty(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, "234");
//        };
//    }

    @Bean
    public Consumer<Flux<String>> consumer() {
        return flux -> flux
                .doOnNext(s -> log.info("consumer received {}", s))
                .subscribe();
    }

    @Bean
    public Function<Flux<String>, Mono<Void>> function() {
        return flux -> flux
                .doOnNext(s -> log.info("function received {}", s))
                .then();
    }

}
