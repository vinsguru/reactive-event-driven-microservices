package com.vinsguru.order.messaging.publisher;

import com.vinsguru.common.events.order.OrderEvent;
import com.vinsguru.common.events.order.OrderStatus;
import com.vinsguru.common.outbox.Outbox;
import com.vinsguru.order.common.dto.PurchaseOrderDto;
import com.vinsguru.order.common.service.OrderEventListener;
import com.vinsguru.order.messaging.mapper.OrderEventMapper;
import com.vinsguru.order.messaging.mapper.OutboxMapper;
import com.vinsguru.order.messaging.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderEventOutboxServiceImpl implements OrderEventListener, OrderEventOutboxService {

    private final Sinks.Many<Outbox<OrderEvent>> sink = Sinks.many().unicast().onBackpressureBuffer();
    private final Flux<Outbox<OrderEvent>> flux = sink.asFlux();
    private final OutboxMapper mapper;
    private final OutboxRepository repository;

    @Override
    public Flux<Outbox<OrderEvent>> publish() {
        return this.repository.findAllByOrderById()
                              .map(this.mapper::toOutboxEvent)
                              .concatWith(flux);
    }

    @Override
    public Mono<Void> onOrderCreated(PurchaseOrderDto dto) {
        return this.save(OrderEventMapper.toOrderCreatedEvent(dto), dto.status());
    }

    @Override
    public Mono<Void> onOrderCancelled(PurchaseOrderDto dto) {
        return this.save(OrderEventMapper.toOrderCancelledEvent(dto), dto.status());
    }

    @Override
    public Mono<Void> onOrderCompleted(PurchaseOrderDto dto) {
        return this.save(OrderEventMapper.toOrderCompletedEvent(dto), dto.status());
    }

    @Override
    public Mono<Void> deleteEvents(List<Long> ids) {
        return this.repository.deleteAllById(ids);
    }

    private Mono<Void> save(OrderEvent event, OrderStatus status) {
        var entity = this.mapper.toEntity(event, status);
        return this.repository.save(entity)
                              .doOnNext(e -> this.sink.tryEmitNext(Outbox.<OrderEvent>builder().correlationId(e.getId()).event(event).build()))
                              .then();
    }

}
