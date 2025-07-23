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

    @Test
    void testEqualsWithNull() {
        CancelPaymentRequest request = new CancelPaymentRequest("order1", 100L, "reason1");
        
        assertNotEquals(null, request);
    }

    @Test
    void testEqualsWithDifferentClass() {
        CancelPaymentRequest request = new CancelPaymentRequest("order1", 100L, "reason1");
        String differentObject = "not a request";
        
        assertNotEquals(request, differentObject);
    }

    @Test
    void testEqualsWithSameInstance() {
        CancelPaymentRequest request = new CancelPaymentRequest("order1", 100L, "reason1");
        
        assertEquals(request, request);
        assertEquals(request.hashCode(), request.hashCode());
    }

    @Test
    void testEqualsWithNullFields() {
        CancelPaymentRequest request1 = new CancelPaymentRequest(null, 0L, null);
        CancelPaymentRequest request2 = new CancelPaymentRequest(null, 0L, null);
        
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testEqualsWithDifferentFields() {
        CancelPaymentRequest request1 = new CancelPaymentRequest("order1", 100L, "reason1");
        CancelPaymentRequest request2 = new CancelPaymentRequest("order2", 100L, "reason1");
        
        assertNotEquals(request1, request2);
        
        request2.setOrderId("order1");
        request2.setAmount(200L);
        assertNotEquals(request1, request2);
        
        request2.setAmount(100L);
        request2.setCancelReason("reason2");
        assertNotEquals(request1, request2);
    }

    @Test
    void testCanEqual() {
        CancelPaymentRequest request1 = new CancelPaymentRequest("order1", 100L, "reason1");
        CancelPaymentRequest request2 = new CancelPaymentRequest("order2", 200L, "reason2");
        Object differentType = new Object();
        
        assertTrue(request1.canEqual(request2));
        assertFalse(request1.canEqual(differentType));
        assertFalse(request1.canEqual(null));
    }
}
