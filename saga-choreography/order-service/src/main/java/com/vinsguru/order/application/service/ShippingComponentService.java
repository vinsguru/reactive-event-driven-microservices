package com.vinsguru.order.application.service;

import com.vinsguru.common.events.order.OrderStatus;
import com.vinsguru.order.application.repository.PurchaseOrderRepository;
import com.vinsguru.order.common.dto.OrderShipmentSchedule;
import com.vinsguru.order.common.service.shipping.ShippingComponentStatusListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ShippingComponentService implements ShippingComponentStatusListener {

    private final PurchaseOrderRepository repository;

    @Override
    public Mono<Void> onSuccess(OrderShipmentSchedule message) {
        return this.repository.findByOrderIdAndStatus(message.orderId(), OrderStatus.COMPLETED)
                              .doOnNext(e -> e.setDeliveryDate(message.deliveryDate()))
                              .flatMap(this.repository::save)
                              .then();
    }

    @Override
    public Mono<Void> onFailure(OrderShipmentSchedule message) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> onRollback(OrderShipmentSchedule message) {
        return Mono.empty();
    }

}
