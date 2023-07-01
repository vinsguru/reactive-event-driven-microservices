package com.vinsguru.order;

import com.vinsguru.common.events.inventory.InventoryEvent;
import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.events.payment.PaymentEvent;
import com.vinsguru.common.events.shipping.ShippingEvent;
import com.vinsguru.order.common.dto.OrderCreateRequest;
import com.vinsguru.order.common.dto.OrderDetails;
import com.vinsguru.order.common.dto.PurchaseOrderDto;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@DirtiesContext
@AutoConfigureWebTestClient
@SpringBootTest(properties = {
        "logging.level.root=ERROR",
        "logging.level.com.vinsguru*=INFO",
        "spring.cloud.stream.kafka.binder.configuration.auto.offset.reset=earliest",
        "spring.cloud.function.definition=orderEventProducer;paymentListener;inventoryListener;shippingListener;orderEventConsumer",
        "spring.cloud.stream.bindings.orderEventConsumer-in-0.destination=order-events"
})
@EmbeddedKafka(
        partitions = 1,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@Import(AbstractIntegrationTest.TestConfig.class)
public abstract class AbstractIntegrationTest {

    private static final Sinks.Many<OrderEvent> resSink = Sinks.many().unicast().onBackpressureBuffer();
    private static final Flux<OrderEvent> resFlux = resSink.asFlux().cache(0);

    @Autowired
    private WebTestClient client;

    @Autowired
    private StreamBridge streamBridge;

    protected void emitEvent(PaymentEvent event) {
        this.streamBridge.send("payment-events", event);
    }

    protected void emitEvent(InventoryEvent event) {
        this.streamBridge.send("inventory-events", event);
    }

    protected void emitEvent(ShippingEvent event) {
        this.streamBridge.send("shipping-events", event);
    }

    protected UUID initiateOrder(OrderCreateRequest request) {
        var orderIdRef = new AtomicReference<UUID>();
        this.client
                .post()
                .uri("/order")
                .bodyValue(request)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.orderId").exists()
                .jsonPath("$.orderId").value(id -> orderIdRef.set(UUID.fromString(id.toString())))
                .jsonPath("$.status").isEqualTo("PENDING");
        return orderIdRef.get();
    }

    protected void verifyOrderDetails(UUID orderId, Consumer<OrderDetails> assertion) {
        this.client
                .get()
                .uri("/order/{orderId}", orderId)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(OrderDetails.class)
                .value(r -> {
                    Assertions.assertEquals(orderId, r.order().orderId());
                    Assertions.assertNotNull(r.inventory());
                    Assertions.assertNotNull(r.payment());
                    assertion.accept(r);
                });
    }

    protected void verifyAllOrders(UUID... orderIds) {
        this.client
                .get()
                .uri("/order/all")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(new ParameterizedTypeReference<List<PurchaseOrderDto>>() {
                })
                .value(r -> {
                    Assertions.assertEquals(List.of(orderIds), r.stream().map(PurchaseOrderDto::orderId).toList());
                });
    }

    protected void verifyOrderCreatedEvent(UUID orderId, int totalAmount) {
        expectEvent(OrderEvent.OrderCreated.class, e -> {
            Assertions.assertEquals(totalAmount, e.totalAmount());
            Assertions.assertEquals(orderId, e.orderId());
        });
    }

    protected void verifyOrderCancelledEvent(UUID orderId) {
        expectEvent(OrderEvent.OrderCancelled.class, e -> Assertions.assertEquals(orderId, e.orderId()));
    }

    protected void verifyOrderCompletedEvent(UUID orderId) {
        expectEvent(OrderEvent.OrderCompleted.class, e -> Assertions.assertEquals(orderId, e.orderId()));
    }

    protected <T> void expectEvent(Class<T> type, Consumer<T> assertion) {
        resFlux
                //.next()
                .timeout(Duration.ofSeconds(2), Mono.empty())
                .cast(type)
                .as(StepVerifier::create)
                .consumeNextWith(assertion)
                .verifyComplete();
    }

    protected void expectNoEvent() {
        resFlux
                .next()
                .timeout(Duration.ofSeconds(2), Mono.empty())
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public Consumer<Flux<OrderEvent>> orderEventConsumer() {
            return f -> f.doOnNext(resSink::tryEmitNext).subscribe();
        }

    }

}
