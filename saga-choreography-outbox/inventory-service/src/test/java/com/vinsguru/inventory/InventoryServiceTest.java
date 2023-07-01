package com.vinsguru.inventory;

import com.vinsguru.common.events.inventory.InventoryEvent;
import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.events.payment.PaymentEvent;
import com.vinsguru.inventory.application.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

@TestPropertySource(properties = {
        "spring.cloud.function.definition=processor;orderEventProducer;inventoryEventConsumer",
        "spring.cloud.stream.bindings.orderEventProducer-out-0.destination=order-events",
        "spring.cloud.stream.bindings.inventoryEventConsumer-in-0.destination=inventory-events"
})
public class InventoryServiceTest extends AbstractIntegrationTest {

    private static final Sinks.Many<OrderEvent> reqSink = Sinks.many().unicast().onBackpressureBuffer();
    private static final Sinks.Many<InventoryEvent> resSink = Sinks.many().unicast().onBackpressureBuffer();
    private static final Flux<InventoryEvent> resFlux = resSink.asFlux().cache(0);

    @Autowired
    private ProductRepository repository;

    @Test
    public void deductAndRestoreTest(){

        // deduct inventory
        var orderCreatedEvent = TestDataUtil.createOrderCreatedEvent(1, 1, 2, 3);
        expectEvent(orderCreatedEvent, InventoryEvent.InventoryDeducted.class, e -> {
            Assertions.assertNotNull(e.inventoryId());
            Assertions.assertEquals(orderCreatedEvent.orderId(), e.orderId());
            Assertions.assertEquals(3, e.quantity());
        });

        // check quantity
        this.repository.findById(1)
                       .as(StepVerifier::create)
                       .consumeNextWith(p -> Assertions.assertEquals(7, p.getAvailableQuantity()))
                       .verifyComplete();

        // duplicate event
        expectNoEvent(orderCreatedEvent);

        // cancelled event & restore
        var cancelledEvent = TestDataUtil.createOrderCancelledEvent(orderCreatedEvent.orderId());
        expectEvent(cancelledEvent, InventoryEvent.InventoryRestored.class, e -> {
            Assertions.assertNotNull(e.inventoryId());
            Assertions.assertEquals(orderCreatedEvent.orderId(), e.orderId());
            Assertions.assertEquals(3, e.quantity());
        });

        // check quantity
        this.repository.findById(1)
                       .as(StepVerifier::create)
                       .consumeNextWith(p -> Assertions.assertEquals(10, p.getAvailableQuantity()))
                       .verifyComplete();

    }

    @Test
    public void restoreWithoutDeductTest(){
        var cancelledEvent = TestDataUtil.createOrderCancelledEvent(UUID.randomUUID());
        expectNoEvent(cancelledEvent);
    }

    @Test
    public void outOfStockErrorTest(){
        var orderCreatedEvent = TestDataUtil.createOrderCreatedEvent(1, 1, 2, 11);
        expectEvent(orderCreatedEvent, InventoryEvent.InventoryDeclined.class, e -> {
            Assertions.assertEquals(orderCreatedEvent.orderId(), e.orderId());
            Assertions.assertEquals(11, e.quantity());
            Assertions.assertEquals("Out of stock", e.message());
        });
    }

    private <T> void expectEvent(OrderEvent event, Class<T> type, Consumer<T> assertion){
        resFlux
                .doFirst(() -> reqSink.tryEmitNext(event))
                .next()
                .timeout(Duration.ofSeconds(1), Mono.empty())
                .cast(type)
                .as(StepVerifier::create)
                .consumeNextWith(assertion)
                .verifyComplete();
    }

    private void expectNoEvent(OrderEvent event){
        resFlux
                .doFirst(() -> reqSink.tryEmitNext(event))
                .next()
                .timeout(Duration.ofSeconds(1), Mono.empty())
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public Supplier<Flux<OrderEvent>> orderEventProducer(){
            return reqSink::asFlux;
        }

        @Bean
        public Consumer<Flux<InventoryEvent>> inventoryEventConsumer(){
            return f -> f.doOnNext(resSink::tryEmitNext).subscribe();
        }

    }


}
