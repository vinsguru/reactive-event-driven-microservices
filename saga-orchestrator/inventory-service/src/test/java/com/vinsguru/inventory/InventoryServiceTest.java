package com.vinsguru.inventory;

import com.vinsguru.common.messages.Request;
import com.vinsguru.common.messages.inventory.InventoryResponse;
import com.vinsguru.inventory.application.entity.Product;
import com.vinsguru.inventory.application.repository.ProductRepository;
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
        "spring.cloud.stream.bindings.responseConsumer-in-0.destination=inventory-response"
})
public class InventoryServiceTest extends AbstractIntegrationTest {

    private static final Sinks.Many<InventoryResponse> resSink = Sinks.many().unicast().onBackpressureBuffer();
    private static final Flux<InventoryResponse> resFlux = resSink.asFlux().cache(0);

    @Autowired
    private ProductRepository repository;

    @Autowired
    private StreamBridge streamBridge;

    @Test
    public void deductAndRestoreTest(){

        var orderId = UUID.randomUUID();
        var deductRequest = TestDataUtil.createDeductRequest(orderId, 1, 3);
        var restoreRequest = TestDataUtil.createRestoreRequest(orderId);

        // deduct inventory
        expectResponse(deductRequest, InventoryResponse.Deducted.class, e -> {
            Assertions.assertNotNull(e.inventoryId());
            Assertions.assertEquals(orderId, e.orderId());
            Assertions.assertEquals(3, e.quantity());
        });

        // check quantity
        this.repository.findById(1)
                       .as(StepVerifier::create)
                       .consumeNextWith(p -> Assertions.assertEquals(7, p.getAvailableQuantity()))
                       .verifyComplete();

        // duplicate request
        expectNoResponse(deductRequest);

        // restore request
        expectNoResponse(restoreRequest);

        // check quantity
        this.repository.findById(1)
                       .as(StepVerifier::create)
                       .consumeNextWith(p -> Assertions.assertEquals(10, p.getAvailableQuantity()))
                       .verifyComplete();

    }

    @Test // please remove this - not a good fit for embedded kafka test. should be covered as part of unit tests
    public void restoreWithoutDeductTest(){
        var orderId = UUID.randomUUID();
        var restoreRequest = TestDataUtil.createRestoreRequest(orderId);
        expectNoResponse(restoreRequest);
        this.repository.findAll()
                       .map(Product::getAvailableQuantity)
                       .distinct()
                       .as(StepVerifier::create)
                       .consumeNextWith(b -> Assertions.assertEquals(10, b))
                       .verifyComplete();
    }

    @Test
    public void outOfStockErrorTest(){
        var orderId = UUID.randomUUID();
        var deductRequest = TestDataUtil.createDeductRequest(orderId, 2, 11);
        expectResponse(deductRequest, InventoryResponse.Declined.class, e -> {
            Assertions.assertEquals(orderId, e.orderId());
            Assertions.assertEquals("Out of stock", e.message());
        });
    }

    private <T> void expectResponse(Request request, Class<T> type, Consumer<T> assertion){
        resFlux
                .doFirst(() -> this.streamBridge.send("inventory-request", request))
                .timeout(Duration.ofSeconds(2), Mono.empty())
                .cast(type)
                .as(StepVerifier::create)
                .consumeNextWith(assertion)
                .verifyComplete();
    }

    private void expectNoResponse(Request request){
        resFlux
                .doFirst(() -> this.streamBridge.send("inventory-request", request))
                .timeout(Duration.ofSeconds(2), Mono.empty())
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public Consumer<Flux<InventoryResponse>> responseConsumer(){
            return f -> f.doOnNext(resSink::tryEmitNext).subscribe();
        }

    }

}
