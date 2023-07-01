package com.vinsguru.order.application.repository;

import com.vinsguru.order.application.entity.OrderWorkflowAction;
import com.vinsguru.order.common.enums.WorkflowAction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface OrderWorkflowActionRepository extends ReactiveCrudRepository<OrderWorkflowAction, UUID> {

    Mono<Boolean> existsByOrderIdAndAction(UUID orderId, WorkflowAction action);

    Flux<OrderWorkflowAction> findByOrderIdOrderByCreatedAt(UUID orderId);

}
