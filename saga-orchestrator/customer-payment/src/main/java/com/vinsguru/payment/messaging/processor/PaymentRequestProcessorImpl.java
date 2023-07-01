package com.vinsguru.payment.messaging.processor;

import com.vinsguru.common.exception.EventAlreadyProcessedException;
import com.vinsguru.common.messages.payment.PaymentRequest;
import com.vinsguru.common.messages.payment.PaymentResponse;
import com.vinsguru.payment.common.service.PaymentService;
import com.vinsguru.payment.messaging.mapper.MessageDtoMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.UnaryOperator;

@Service
@RequiredArgsConstructor
public class PaymentRequestProcessorImpl implements PaymentRequestProcessor {

    private final PaymentService service;

    @Override
    public Mono<PaymentResponse> handle(PaymentRequest.Process request) {
        var dto = MessageDtoMapper.toProcessRequest(request);
        return this.service.process(dto)
                           .map(MessageDtoMapper::toProcessedResponse)
                           .transform(exceptionHandler(request));
    }

    @Override
    public Mono<PaymentResponse> handle(PaymentRequest.Refund request) {
        return this.service.refund(request.orderId())
                           .then(Mono.empty());
    }

    private UnaryOperator<Mono<PaymentResponse>> exceptionHandler(PaymentRequest.Process request) {
        return mono -> mono.onErrorResume(EventAlreadyProcessedException.class, ex -> Mono.empty())
                           .onErrorResume(MessageDtoMapper.toPaymentDeclinedResponse(request));
    }

}
