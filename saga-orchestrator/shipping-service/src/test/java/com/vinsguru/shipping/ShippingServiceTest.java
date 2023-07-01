package com.vinsguru.shipping;

import com.vinsguru.common.messages.Request;
import com.vinsguru.common.messages.shipping.ShippingResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Consumer;

@TestPropertySource(properties = {
        "spring.cloud.function.definition=processor;responseConsumer",
        "spring.cloud.stream.bindings.responseConsumer-in-0.destination=shipping-response"
})
public class ShippingServiceTest extends AbstractIntegrationTest {

    private static final Sinks.Many<ShippingResponse> resSink = Sinks.many().unicast().onBackpressureBuffer();
    private static final Flux<ShippingResponse> resFlux = resSink.asFlux().cache(0);

    @Autowired
    private StreamBridge streamBridge;

    @Test
    public void scheduleTest(){
        var orderId = UUID.randomUUID();
        var request = TestDataUtil.createScheduleRequest(orderId, 1, 1, 1);

        expectResponse(request, ShippingResponse.Scheduled.class, r -> {
            Assertions.assertEquals(orderId, r.orderId());
            Assertions.assertNotNull(r.deliveryDate());
            Assertions.assertNotNull(r.shipmentId());
        });

        // duplicate request
        expectNoResponse(request);

    }

    @Test
    public void declineTest(){
        var orderId = UUID.randomUUID();
        var request = TestDataUtil.createScheduleRequest(orderId, 1, 1, 10);

        expectResponse(request, ShippingResponse.Declined.class, r -> {
            Assertions.assertEquals(orderId, r.orderId());
            Assertions.assertEquals("Shipment quantity exceeded the limit", r.message());
        });

    }

    private <T> void expectResponse(Request request, Class<T> type, Consumer<T> assertion){
        resFlux
                .doFirst(() -> this.streamBridge.send("shipping-request", request))
                .timeout(Duration.ofSeconds(2), Mono.empty())
                .cast(type)
                .as(StepVerifier::create)
                .consumeNextWith(assertion)
                .verifyComplete();
    }

    private void expectNoResponse(Request request){
        resFlux
                .doFirst(() -> this.streamBridge.send("shipping-request", request))
                .timeout(Duration.ofSeconds(2), Mono.empty())
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public Consumer<Flux<ShippingResponse>> responseConsumer(){
            return f -> f.doOnNext(resSink::tryEmitNext).subscribe();
        }

    }

}
