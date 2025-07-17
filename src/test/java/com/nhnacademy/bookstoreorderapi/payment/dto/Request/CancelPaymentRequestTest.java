package com.nhnacademy.bookstoreorderapi.payment.dto.Request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CancelPaymentRequestTest {

    @Test
    void testConstructorAndGetters() {
        String orderId = "order123";
        Long amount = 1000L;
        String cancelReason = "Customer changed mind";

        CancelPaymentRequest request = new CancelPaymentRequest(orderId, amount, cancelReason);

        assertEquals(orderId, request.getOrderId());
        assertEquals(amount, request.getAmount());
        assertEquals(cancelReason, request.getCancelReason());
    }

    @Test
    void testSetters() {
        CancelPaymentRequest request = new CancelPaymentRequest(null, 0L, null);

        String orderId = "order456";
        long amount = 2000L;
        String cancelReason = "Item out of stock";

        request.setOrderId(orderId);
        request.setAmount(amount);
        request.setCancelReason(cancelReason);

        assertEquals(orderId, request.getOrderId());
        assertEquals(amount, request.getAmount());
        assertEquals(cancelReason, request.getCancelReason());
    }

    @Test
    void testEqualsAndHashCode() {
        CancelPaymentRequest request1 = new CancelPaymentRequest("order1", 100L, "reason1");
        CancelPaymentRequest request2 = new CancelPaymentRequest("order1", 100L, "reason1");
        CancelPaymentRequest request3 = new CancelPaymentRequest("order2", 200L, "reason2");

        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1.hashCode(), request3.hashCode());
    }

    @Test
    void testToString() {
        CancelPaymentRequest request = new CancelPaymentRequest("order1", 100L, "reason1");
        String toString = request.toString();
        assertTrue(toString.contains("orderId=order1"));
        assertTrue(toString.contains("amount=100"));
        assertTrue(toString.contains("cancelReason=reason1"));
    }
}
