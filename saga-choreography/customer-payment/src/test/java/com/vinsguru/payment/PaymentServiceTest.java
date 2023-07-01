package com.vinsguru.payment;

import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.events.payment.PaymentEvent;
import com.vinsguru.payment.application.repository.CustomerRepository;
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
        "spring.cloud.function.definition=processor;orderEventProducer;paymentEventConsumer",
        "spring.cloud.stream.bindings.orderEventProducer-out-0.destination=order-events",
        "spring.cloud.stream.bindings.paymentEventConsumer-in-0.destination=payment-events"
})
public class PaymentServiceTest extends AbstractIntegrationTest {

    private static final Sinks.Many<OrderEvent> reqSink = Sinks.many().unicast().onBackpressureBuffer();
    private static final Sinks.Many<PaymentEvent> resSink = Sinks.many().unicast().onBackpressureBuffer();
    private static final Flux<PaymentEvent> resFlux = resSink.asFlux().cache(0);

    @Autowired
    private CustomerRepository repository;

    @Test
    public void deductAndRefundTest(){

        // deduct payment
        var orderCreatedEvent = TestDataUtil.createOrderCreatedEvent(1, 1, 2, 3);
        expectEvent(orderCreatedEvent, PaymentEvent.PaymentDeducted.class, e -> {
            Assertions.assertNotNull(e.paymentId());
            Assertions.assertEquals(orderCreatedEvent.orderId(), e.orderId());
            Assertions.assertEquals(6, e.amount());
        });

        // check balance
        this.repository.findById(1)
                       .as(StepVerifier::create)
                       .consumeNextWith(c -> Assertions.assertEquals(94, c.getBalance()))
                       .verifyComplete();

        // duplicate event
        expectNoEvent(orderCreatedEvent);

        // cancelled event & refund
        var cancelledEvent = TestDataUtil.createOrderCancelledEvent(orderCreatedEvent.orderId());
        expectEvent(cancelledEvent, PaymentEvent.PaymentRefunded.class, e -> {
            Assertions.assertNotNull(e.paymentId());
            Assertions.assertEquals(orderCreatedEvent.orderId(), e.orderId());
            Assertions.assertEquals(6, e.amount());
        });

        // check balance
        this.repository.findById(1)
                       .as(StepVerifier::create)
                       .consumeNextWith(c -> Assertions.assertEquals(100, c.getBalance()))
                       .verifyComplete();

    }

    @Test
    public void refundWithoutDeductTest(){
        var cancelledEvent = TestDataUtil.createOrderCancelledEvent(UUID.randomUUID());
        expectNoEvent(cancelledEvent);
    }

    @Test
    public void customerNotFoundTest(){
        var orderCreatedEvent = TestDataUtil.createOrderCreatedEvent(10, 1, 2, 3);
        expectEvent(orderCreatedEvent, PaymentEvent.PaymentDeclined.class, e -> {
            Assertions.assertEquals(orderCreatedEvent.orderId(), e.orderId());
            Assertions.assertEquals(6, e.amount());
            Assertions.assertEquals("Customer not found", e.message());
        });
    }

    @Test
    public void insufficientBalanceTest(){
        var orderCreatedEvent = TestDataUtil.createOrderCreatedEvent(1, 1, 2, 51);
        expectEvent(orderCreatedEvent, PaymentEvent.PaymentDeclined.class, e -> {
            Assertions.assertEquals(orderCreatedEvent.orderId(), e.orderId());
            Assertions.assertEquals(102, e.amount());
            Assertions.assertEquals("Customer does not have enough balance", e.message());
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
        public Consumer<Flux<PaymentEvent>> paymentEventConsumer(){
            return f -> f.doOnNext(resSink::tryEmitNext).subscribe();
        }

    }


}
