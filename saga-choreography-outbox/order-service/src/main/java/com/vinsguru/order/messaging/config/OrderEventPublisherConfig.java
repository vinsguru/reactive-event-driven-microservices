package com.vinsguru.order.messaging.config;

import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.outbox.Outbox;
import com.vinsguru.order.messaging.publisher.OrderEventOutboxService;
import lombok.RequiredArgsConstructor;
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
import reactor.kafka.sender.SenderResult;

import java.time.Duration;
import java.util.function.Supplier;

@Configuration
@RequiredArgsConstructor
public class OrderEventPublisherConfig {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisherConfig.class);
    private final OrderEventOutboxService orderEventOutboxService;

    @Bean
    @SuppressWarnings("unchecked")
    public FluxMessageChannel orderEventResults() {
        var channel = new FluxMessageChannel();
        Flux.from(channel)
            .map(m -> (SenderResult<Long>) m.getPayload())
            .map(SenderResult::correlationMetadata)
            .bufferTimeout(1000, Duration.ofMillis(100)) // use config
            .doOnNext(list -> log.info("deleting ids {}", list))
            .flatMap(this.orderEventOutboxService::deleteEvents)
            .subscribe();
        return channel;
    }

    @Bean
    public Supplier<Flux<Message<OrderEvent>>> orderEventProducer() {
        return () -> this.orderEventOutboxService.publish()
                                                 //   .repeatWhen(f -> f.delayElements(Duration.ofMillis(1000))) // use config
                                                 .map(this::toMessage);
    }

    private Message<OrderEvent> toMessage(Outbox<OrderEvent> outbox) {
        log.info("order service produced {}", outbox.event());
        return MessageBuilder.withPayload(outbox.event())
                             .setHeader(KafkaHeaders.KEY, outbox.event().orderId().toString())
                             .setHeader(IntegrationMessageHeaderAccessor.CORRELATION_ID, outbox.correlationId())
                             .build();
    }

}
