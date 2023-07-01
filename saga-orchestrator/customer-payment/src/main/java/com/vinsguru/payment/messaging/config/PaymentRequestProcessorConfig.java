package com.vinsguru.payment.messaging.config;

import com.vinsguru.common.messages.payment.PaymentRequest;
import com.vinsguru.common.messages.payment.PaymentResponse;
import com.vinsguru.common.util.MessageConverter;
import com.vinsguru.payment.messaging.processor.PaymentRequestProcessor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
public class PaymentRequestProcessorConfig {

    private static final Logger log = LoggerFactory.getLogger(PaymentRequestProcessorConfig.class);
    private final PaymentRequestProcessor paymentRequestProcessor;

    @Bean
    public Function<Flux<Message<PaymentRequest>>, Flux<Message<PaymentResponse>>> processor() {
        return flux -> flux.map(MessageConverter::toRecord)
                           .doOnNext(r -> log.info("customer payment received {}", r.message()))
                           .concatMap(r -> this.paymentRequestProcessor.process(r.message())
                                                                       .doOnSuccess(e -> r.acknowledgement().acknowledge())
                                                                       .doOnError(e -> log.error(e.getMessage()))
                           )
                           .map(this::toMessage);
    }

    private Message<PaymentResponse> toMessage(PaymentResponse response) {
        log.info("customer payment produced {}", response);
        return MessageBuilder.withPayload(response)
                             .setHeader(KafkaHeaders.KEY, response.orderId().toString())
                             .build();
    }

}
