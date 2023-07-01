package com.vinsguru.payment;

import com.vinsguru.common.messages.Request;
import com.vinsguru.common.messages.payment.PaymentResponse;
import com.vinsguru.payment.application.entity.Customer;
import com.vinsguru.payment.application.repository.CustomerRepository;
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
        "spring.cloud.stream.bindings.responseConsumer-in-0.destination=payment-response"
})
public class PaymentServiceTest extends AbstractIntegrationTest {

    private static final Sinks.Many<PaymentResponse> resSink = Sinks.many().unicast().onBackpressureBuffer();
    private static final Flux<PaymentResponse> resFlux = resSink.asFlux().cache(0);

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private StreamBridge streamBridge;

    @Test
    public void processAndRefundTest(){

        var orderId = UUID.randomUUID();
        var processRequest = TestDataUtil.createProcessRequest(orderId, 1, 3);
        var refundRequest = TestDataUtil.createRefundRequest(orderId);

        // process payment
        expectResponse(processRequest, PaymentResponse.Processed.class, e -> {
            Assertions.assertNotNull(e.paymentId());
            Assertions.assertEquals(orderId, e.orderId());
            Assertions.assertEquals(3, e.amount());
        });

        // check balance
        this.repository.findById(1)
                       .as(StepVerifier::create)
                       .consumeNextWith(c -> Assertions.assertEquals(97, c.getBalance()))
                       .verifyComplete();

        // duplicate request
        expectNoResponse(processRequest);

        // refund request
        expectNoResponse(refundRequest);

        // check balance
        this.repository.findById(1)
                       .as(StepVerifier::create)
                       .consumeNextWith(c -> Assertions.assertEquals(100, c.getBalance()))
                       .verifyComplete();

    }

    @Test // please remove this. should be covered as part of unit tests
    public void refundWithoutProcessTest(){
        var orderId = UUID.randomUUID();
        var refundRequest = TestDataUtil.createRefundRequest(orderId);
        expectNoResponse(refundRequest);
        this.repository.findAll()
                       .map(Customer::getBalance)
                       .distinct()
                       .as(StepVerifier::create)
                       .consumeNextWith(b -> Assertions.assertEquals(100, b))
                       .verifyComplete();
    }

    @Test
    public void customerNotFoundTest(){
        var orderId = UUID.randomUUID();
        var processRequest = TestDataUtil.createProcessRequest(orderId, 10, 3);
        expectResponse(processRequest, PaymentResponse.Declined.class, e -> {
            Assertions.assertEquals(orderId, e.orderId());
            Assertions.assertEquals("Customer not found", e.message());
        });
    }

    @Test
    public void insufficientBalanceTest(){
        var orderId = UUID.randomUUID();
        var processRequest = TestDataUtil.createProcessRequest(orderId, 2, 101);
        expectResponse(processRequest, PaymentResponse.Declined.class, e -> {
            Assertions.assertEquals(orderId, e.orderId());
            Assertions.assertEquals("Customer does not have enough balance", e.message());
        });
    }

    private <T> void expectResponse(Request request, Class<T> type, Consumer<T> assertion){
        resFlux
                .doFirst(() -> this.streamBridge.send("payment-request", request))
                .timeout(Duration.ofSeconds(2), Mono.empty())
                .cast(type)
                .as(StepVerifier::create)
                .consumeNextWith(assertion)
                .verifyComplete();
    }

    private void expectNoResponse(Request request){
        resFlux
                .doFirst(() -> this.streamBridge.send("payment-request", request))
                .timeout(Duration.ofSeconds(2), Mono.empty())
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public Consumer<Flux<PaymentResponse>> responseConsumer(){
            return f -> f.doOnNext(resSink::tryEmitNext).subscribe();
        }

    }

}
