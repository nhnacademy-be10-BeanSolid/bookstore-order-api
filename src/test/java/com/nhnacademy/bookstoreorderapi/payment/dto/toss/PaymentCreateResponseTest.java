package com.nhnacademy.bookstoreorderapi.payment.dto.toss;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentCreateResponseTest {

    @Test
    void testConstructorAndGetters() {
        String paymentKey = "testPaymentKey";
        PaymentCreateResponse response = new PaymentCreateResponse();
        response.setPaymentKey(paymentKey);

        assertEquals(paymentKey, response.getPaymentKey());
    }

    @Test
    void testSetters() {
        PaymentCreateResponse response = new PaymentCreateResponse();
        String paymentKey = "newPaymentKey";
        response.setPaymentKey(paymentKey);

        assertEquals(paymentKey, response.getPaymentKey());
    }

    @Test
    void testEqualsAndHashCode() {
        PaymentCreateResponse response1 = new PaymentCreateResponse();
        response1.setPaymentKey("key1");
        PaymentCreateResponse response2 = new PaymentCreateResponse();
        response2.setPaymentKey("key1");
        PaymentCreateResponse response3 = new PaymentCreateResponse();
        response3.setPaymentKey("key2");

        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }

    @Test
    void testToString() {
        PaymentCreateResponse response = new PaymentCreateResponse();
        response.setPaymentKey("key1");
        String toString = response.toString();

        assertTrue(toString.contains("paymentKey=key1"));
    }
}
