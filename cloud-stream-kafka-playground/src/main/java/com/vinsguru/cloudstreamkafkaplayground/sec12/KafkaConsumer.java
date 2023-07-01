package com.vinsguru.cloudstreamkafkaplayground.sec12;

/*
    goal: to consume messages from multiple topics.
    subscription(List.of(topic1, topic2))
 */

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.reactorkafka.ReceiverOptionsCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.function.Consumer;

@Configuration
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

//    @Bean
//    public ReceiverOptionsCustomizer customizer(){
//        return (s, ro) -> {
//            return ro.consumerProperty(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, "234");
//        };
//    }


    @Bean
    public Consumer<Flux<String>> consumer() {
        return flux -> flux
                .doOnNext(s -> log.info("consumer received {}", s))
                .subscribe();
    }

}
