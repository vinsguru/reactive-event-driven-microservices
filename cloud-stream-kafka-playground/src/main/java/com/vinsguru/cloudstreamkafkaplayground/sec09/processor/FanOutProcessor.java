package com.vinsguru.cloudstreamkafkaplayground.sec09.processor;

import com.vinsguru.cloudstreamkafkaplayground.common.MessageConverter;
import com.vinsguru.cloudstreamkafkaplayground.sec09.dto.DigitalDelivery;
import com.vinsguru.cloudstreamkafkaplayground.sec09.dto.OrderEvent;
import com.vinsguru.cloudstreamkafkaplayground.sec09.dto.OrderType;
import com.vinsguru.cloudstreamkafkaplayground.sec09.dto.PhysicalDelivery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.function.Function;

@Configuration
public class FanOutProcessor {

    private final Sinks.Many<OrderEvent> sink = Sinks.many().multicast().onBackpressureBuffer();

    @Bean
    public Function<Flux<Message<OrderEvent>>, Tuple2<Flux<DigitalDelivery>, Flux<PhysicalDelivery>>> processor() {
        return flux -> {
            flux
                    .map(MessageConverter::toRecord)
                    .doOnNext(r -> this.sink.tryEmitNext(r.message()))
                    .doOnNext(r -> r.acknowledgement().acknowledge())
                    .subscribe();
            return Tuples.of(
                    sink.asFlux().transform(toDigitalDelivery()),
                    sink.asFlux().filter(oe -> OrderType.PHYSICAL.equals(oe.orderType())).transform(toPhysicalDelivery())
            );
        };
    }

    private Function<Flux<OrderEvent>, Flux<DigitalDelivery>> toDigitalDelivery() {
        return flux -> flux.map(e -> new DigitalDelivery(e.productId(), "%s@gmail.com".formatted(e.customerId())));
    }

    private Function<Flux<OrderEvent>, Flux<PhysicalDelivery>> toPhysicalDelivery() {
        return flux -> flux.map(e -> new PhysicalDelivery(
                e.productId(),
                "%s street".formatted(e.customerId()),
                "%s city".formatted(e.customerId()),
                "some country"
        ));
    }

}
