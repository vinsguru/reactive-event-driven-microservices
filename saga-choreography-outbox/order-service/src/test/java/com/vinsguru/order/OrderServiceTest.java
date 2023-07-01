package com.vinsguru.order;

import com.vinsguru.common.events.inventory.InventoryEvent;
import com.vinsguru.common.events.inventory.InventoryStatus;
import com.vinsguru.common.events.order.OrderStatus;
import com.vinsguru.common.events.payment.PaymentEvent;
import com.vinsguru.common.events.payment.PaymentStatus;
import com.vinsguru.common.events.shipping.ShippingEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Instant;


public class OrderServiceTest extends AbstractIntegrationTest {

    @Test
    public void orderCompletedWorkflowTest() throws InterruptedException {
        // order create request
        var request = TestDataUtil.toRequest(1, 1, 2, 3);

        // validate order in pending state
        var orderId = this.initiateOrder(request);

        // check for order created event
        this.verifyOrderCreatedEvent(orderId, 6);

        // emit payment deducted event
        this.emitEvent(PaymentEvent.PaymentDeducted.builder().orderId(orderId).build());

        // emit inventory deducted event
        this.emitEvent(InventoryEvent.InventoryDeducted.builder().orderId(orderId).build());

        // check for order completed event
        this.verifyOrderCompletedEvent(orderId);

        // emit shipping scheduled event
        this.emitEvent(ShippingEvent.ShippingScheduled.builder().orderId(orderId).expectedDelivery(Instant.now()).build());

        // verify the order details via REST endpoint
        // we might have to wait for sometime for stream bridge to send and app to process
        Thread.sleep(1500);
        // if you do not like this hard coded wait, plz autowire the processor bean as discussed

        this.verifyOrderDetails(orderId, r -> {
            Assertions.assertNotNull(r.order().deliveryDate());
            Assertions.assertEquals(OrderStatus.COMPLETED, r.order().status());
            Assertions.assertEquals(PaymentStatus.DEDUCTED, r.payment().status());
            Assertions.assertEquals(InventoryStatus.DEDUCTED, r.inventory().status());
        });

    }

    @Test
    public void orderCancelledWhenInventoryDeclinedTest() throws InterruptedException {
        // order create request
        var request = TestDataUtil.toRequest(1, 1, 2, 3);

        // validate order in pending state
        var orderId = this.initiateOrder(request);

        // check for order created event
        this.verifyOrderCreatedEvent(orderId, 6);

        // emit payment deducted event
        this.emitEvent(PaymentEvent.PaymentDeducted.builder().orderId(orderId).build());

        // emit inventory declined event
        this.emitEvent(InventoryEvent.InventoryDeclined.builder().orderId(orderId).build());

        // check for order cancelled event
        this.verifyOrderCancelledEvent(orderId);

        // emit shipping scheduled event
        this.emitEvent(ShippingEvent.ShippingScheduled.builder().orderId(orderId).expectedDelivery(Instant.now()).build());

        // verify the order details via REST endpoint
        // we might have to wait for sometime for stream bridge to send and app to process
        Thread.sleep(1500);
        // if you do not like this hard coded wait, plz autowire the processor bean as discussed

        this.verifyOrderDetails(orderId, r -> {
            Assertions.assertNull(r.order().deliveryDate());
            Assertions.assertEquals(OrderStatus.CANCELLED, r.order().status());
            Assertions.assertEquals(PaymentStatus.DEDUCTED, r.payment().status());
            Assertions.assertEquals(InventoryStatus.DECLINED, r.inventory().status());
        });

    }

    @Test
    public void verifyCompensatingTransaction() throws InterruptedException {
        // order create request
        var request = TestDataUtil.toRequest(1, 1, 2, 3);

        // validate order in pending state
        var orderId = this.initiateOrder(request);

        // check for order created event
        this.verifyOrderCreatedEvent(orderId, 6);

        // emit payment declined event
        this.emitEvent(PaymentEvent.PaymentDeclined.builder().orderId(orderId).build());

        // emit inventory deducted event
        this.emitEvent(InventoryEvent.InventoryDeducted.builder().orderId(orderId).build());

        // check for order cancelled event
        this.verifyOrderCancelledEvent(orderId);

        // emit inventory restored event
        this.emitEvent(InventoryEvent.InventoryRestored.builder().orderId(orderId).build());

        // verify the order details via REST endpoint
        // we might have to wait for sometime for stream bridge to send and app to process
        Thread.sleep(1500);
        // if you do not like this hard coded wait, plz autowire the processor bean as discussed

        this.verifyOrderDetails(orderId, r -> {
            Assertions.assertNull(r.order().deliveryDate());
            Assertions.assertEquals(OrderStatus.CANCELLED, r.order().status());
            Assertions.assertEquals(PaymentStatus.DECLINED, r.payment().status());
            Assertions.assertEquals(InventoryStatus.RESTORED, r.inventory().status());
        });

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void getAllOrdersTest() {
        // order create request
        var request = TestDataUtil.toRequest(1, 1, 2, 3);

        // validate order in pending state
        var orderId1 = this.initiateOrder(request);

        // check for order created event
        this.verifyOrderCreatedEvent(orderId1, 6);

        // verify if GET all orders API returns one item in the response
        this.verifyAllOrders(orderId1);

        // validate order in pending state
        var orderId2 = this.initiateOrder(request);

        // check for order created event
        this.verifyOrderCreatedEvent(orderId2, 6);

        // verify if GET all orders API returns 2 items in the response
        this.verifyAllOrders(orderId1, orderId2);

    }


}
