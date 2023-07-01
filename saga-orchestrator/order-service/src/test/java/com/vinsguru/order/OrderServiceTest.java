package com.vinsguru.order;

import com.vinsguru.common.messages.inventory.InventoryRequest;
import com.vinsguru.common.messages.inventory.InventoryResponse;
import com.vinsguru.common.messages.payment.PaymentRequest;
import com.vinsguru.common.messages.payment.PaymentResponse;
import com.vinsguru.common.messages.shipping.ShippingResponse;
import com.vinsguru.order.common.dto.OrderWorkflowActionDto;
import com.vinsguru.order.common.enums.OrderStatus;
import com.vinsguru.order.common.enums.WorkflowAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Instant;
import java.util.List;


public class OrderServiceTest extends AbstractIntegrationTest {

    @Test
    public void orderCompletedWorkflowTest() {
        // order create request
        var request = TestDataUtil.toRequest(1, 1, 2, 3);

        // validate order in pending state
        var orderId = this.initiateOrder(request);

        // check for payment request
        this.verifyPaymentRequest(orderId, 6);

        // emit payment processed response
        this.emitResponse(PaymentResponse.Processed.builder().orderId(orderId).build());

        // check for inventory request
        this.verifyInventoryRequest(orderId, 3);

        // emit inventory deducted response
        this.emitResponse(InventoryResponse.Deducted.builder().orderId(orderId).build());

        // check for schedule request
        this.verifyScheduleRequest(orderId, 3);

        // emit shipping scheduled response
        this.emitResponse(ShippingResponse.Scheduled.builder().orderId(orderId).deliveryDate(Instant.now()).build());

        // check for no request
        this.expectNoRequest();

        this.verifyOrderDetails(orderId, r -> {
            Assertions.assertNotNull(r.order().deliveryDate());
            Assertions.assertEquals(OrderStatus.COMPLETED, r.order().status());
            Assertions.assertFalse(r.actions().isEmpty());
            var expected = List.of(
                    WorkflowAction.PAYMENT_REQUEST_INITIATED,
                    WorkflowAction.PAYMENT_PROCESSED,
                    WorkflowAction.INVENTORY_REQUEST_INITIATED,
                    WorkflowAction.INVENTORY_DEDUCTED,
                    WorkflowAction.SHIPPING_SCHEDULE_INITIATED,
                    WorkflowAction.SHIPPING_SCHEDULED
            );
            Assertions.assertEquals(expected, r.actions().stream().map(OrderWorkflowActionDto::action).toList());
        });

    }

    @Test
    public void orderCancelledWhenPaymentDeclinedTest() {
        // order create request
        var request = TestDataUtil.toRequest(1, 1, 2, 3);

        // validate order in pending state
        var orderId = this.initiateOrder(request);

        // check for payment request
        this.verifyPaymentRequest(orderId, 6);

        // emit payment declined response
        this.emitResponse(PaymentResponse.Declined.builder().orderId(orderId).build());

        // check for no request
        this.expectNoRequest();

        this.verifyOrderDetails(orderId, r -> {
            Assertions.assertNull(r.order().deliveryDate());
            Assertions.assertEquals(OrderStatus.CANCELLED, r.order().status());
            Assertions.assertFalse(r.actions().isEmpty());
            var expected = List.of(
                    WorkflowAction.PAYMENT_REQUEST_INITIATED,
                    WorkflowAction.PAYMENT_DECLINED
            );
            Assertions.assertEquals(expected, r.actions().stream().map(OrderWorkflowActionDto::action).toList());
        });

    }

    @Test
    public void orderCancelledWhenInventoryDeclinedTest() {
        // order create request
        var request = TestDataUtil.toRequest(1, 1, 2, 3);

        // validate order in pending state
        var orderId = this.initiateOrder(request);

        // check for payment request
        this.verifyPaymentRequest(orderId, 6);

        // emit payment processed response
        this.emitResponse(PaymentResponse.Processed.builder().orderId(orderId).build());

        // check for inventory request
        this.verifyInventoryRequest(orderId, 3);

        // emit inventory declined response
        this.emitResponse(InventoryResponse.Declined.builder().orderId(orderId).build());

        // check for refund request
        this.expectRequest(PaymentRequest.Refund.class, r -> Assertions.assertEquals(orderId, r.orderId()));

        this.verifyOrderDetails(orderId, r -> {
            Assertions.assertNull(r.order().deliveryDate());
            Assertions.assertEquals(OrderStatus.CANCELLED, r.order().status());
            Assertions.assertFalse(r.actions().isEmpty());
            var expected = List.of(
                    WorkflowAction.PAYMENT_REQUEST_INITIATED,
                    WorkflowAction.PAYMENT_PROCESSED,
                    WorkflowAction.INVENTORY_REQUEST_INITIATED,
                    WorkflowAction.INVENTORY_DECLINED,
                    WorkflowAction.PAYMENT_REFUND_INITIATED
            );
            Assertions.assertEquals(expected, r.actions().stream().map(OrderWorkflowActionDto::action).toList());
        });

    }

    @Test
    public void orderCancelledWhenShippingDeclinedTest() {
        // order create request
        var request = TestDataUtil.toRequest(1, 1, 2, 3);

        // validate order in pending state
        var orderId = this.initiateOrder(request);

        // check for payment request
        this.verifyPaymentRequest(orderId, 6);

        // emit payment processed response
        this.emitResponse(PaymentResponse.Processed.builder().orderId(orderId).build());

        // check for inventory request
        this.verifyInventoryRequest(orderId, 3);

        // emit inventory deducted response
        this.emitResponse(InventoryResponse.Deducted.builder().orderId(orderId).build());

        // check for schedule request
        this.verifyScheduleRequest(orderId, 3);

        // emit shipping declined response
        this.emitResponse(ShippingResponse.Declined.builder().orderId(orderId).build());

        // check for refund and restore requests
        this.expectRequests(list -> {
            var refund = PaymentRequest.Refund.builder().orderId(orderId).build();
            var restore = InventoryRequest.Restore.builder().orderId(orderId).build();
            Assertions.assertEquals(List.of(restore, refund), list);
        });

        this.verifyOrderDetails(orderId, r -> {
            Assertions.assertNull(r.order().deliveryDate());
            Assertions.assertEquals(OrderStatus.CANCELLED, r.order().status());
            Assertions.assertFalse(r.actions().isEmpty());
            var expected = List.of(
                    WorkflowAction.PAYMENT_REQUEST_INITIATED,
                    WorkflowAction.PAYMENT_PROCESSED,
                    WorkflowAction.INVENTORY_REQUEST_INITIATED,
                    WorkflowAction.INVENTORY_DEDUCTED,
                    WorkflowAction.SHIPPING_SCHEDULE_INITIATED,
                    WorkflowAction.SHIPPING_DECLINED,
                    WorkflowAction.INVENTORY_RESTORE_INITIATED,
                    WorkflowAction.PAYMENT_REFUND_INITIATED
            );
            Assertions.assertEquals(expected, r.actions().stream().map(OrderWorkflowActionDto::action).toList());
        });

    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void getAllOrdersTest(){
        // order create request
        var request = TestDataUtil.toRequest(1, 1, 2, 3);

        // validate order in pending state
        var orderId1 = this.initiateOrder(request);

        // check for payment request
        this.verifyPaymentRequest(orderId1, 6);

        // verify if GET all orders API returns one item in the response
        this.verifyAllOrders(orderId1);

        // place another order
        var orderId2 = this.initiateOrder(request);

        // check for payment request
        this.verifyPaymentRequest(orderId2, 6);

        // verify if GET all orders API returns 2 items in the response
        this.verifyAllOrders(orderId1, orderId2);

    }

}
