package com.vinsguru.cloudstreamkafkaplayground.sec13;

import com.vinsguru.cloudstreamkafkaplayground.AbstractIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

@TestPropertySource(properties = {
        "sec=sec13"
})
@ExtendWith(OutputCaptureExtension.class)
public class SenderResultsTest extends AbstractIntegrationTest {
    /*
        just a demo to show that we are receiving sender results
     */
    @Test
    public void senderResultsTest(CapturedOutput output){
        Mono.delay(Duration.ofSeconds(2))
                .then(Mono.fromSupplier(() -> output))
                .as(StepVerifier::create)
                .consumeNextWith(out -> Assertions.assertTrue(out.getOut().contains("received result id 0, record metadata input-topic-0@0")))
                .verifyComplete();
    }

}
