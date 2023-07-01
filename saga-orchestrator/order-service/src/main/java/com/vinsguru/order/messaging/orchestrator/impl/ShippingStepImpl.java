package com.vinsguru.order.messaging.orchestrator.impl;

import com.vinsguru.common.messages.Request;
import com.vinsguru.common.messages.shipping.ShippingResponse;
import com.vinsguru.common.orchestrator.RequestCompensator;
import com.vinsguru.common.orchestrator.RequestSender;
import com.vinsguru.order.common.enums.WorkflowAction;
import com.vinsguru.order.common.service.OrderFulfillmentService;
import com.vinsguru.order.common.service.WorkflowActionTracker;
import com.vinsguru.order.messaging.mapper.MessageDtoMapper;
import com.vinsguru.order.messaging.orchestrator.ShippingStep;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShippingStepImpl implements ShippingStep {

    private final WorkflowActionTracker tracker;
    private final OrderFulfillmentService service;
    private RequestCompensator previousStep;
    private RequestSender nextStep;

    @Override
    public Publisher<Request> compensate(UUID orderId) {
        return this.previousStep.compensate(orderId);
    }

    @Override
    public Publisher<Request> send(UUID orderId) {
        return this.tracker.track(orderId, WorkflowAction.SHIPPING_SCHEDULE_INITIATED)
                           .then(this.service.get(orderId))
                           .map(MessageDtoMapper::toShippingScheduleRequest);
    }

    @Override
    public void setPreviousStep(RequestCompensator previousStep) {
        this.previousStep = previousStep;
    }

    @Override
    public void setNextStep(RequestSender nextStep) {
        this.nextStep = nextStep;
    }

    @Override
    public Publisher<Request> onSuccess(ShippingResponse.Scheduled response) {
        return this.tracker.track(response.orderId(), WorkflowAction.SHIPPING_SCHEDULED)
                           .thenReturn(MessageDtoMapper.toShipmentSchedule(response))
                           .flatMap(this.service::schedule)
                           .thenMany(this.nextStep.send(response.orderId()));
    }

    @Override
    public Publisher<Request> onFailure(ShippingResponse.Declined response) {
        return this.tracker.track(response.orderId(), WorkflowAction.SHIPPING_DECLINED)
                           .thenMany(this.previousStep.compensate(response.orderId()));
    }
}
