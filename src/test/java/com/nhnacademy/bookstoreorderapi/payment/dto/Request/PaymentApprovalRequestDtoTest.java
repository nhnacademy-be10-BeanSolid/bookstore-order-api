package com.nhnacademy.bookstoreorderapi.payment.dto.Request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PaymentApprovalRequestDtoTest {

    @Test
    void testConstructorAndGetters() {
        String paymentKey = "payment_key_123";
        String orderId = "order_456";
        long amount = 10000L;

        PaymentApprovalRequestDto dto = new PaymentApprovalRequestDto(paymentKey, orderId, amount);

        assertEquals(paymentKey, dto.getPaymentKey());
        assertEquals(orderId, dto.getOrderId());
        assertEquals(amount, dto.getAmount());
    }

    @Test
    void testEqualsWithNull() {
        PaymentApprovalRequestDto dto = new PaymentApprovalRequestDto("key", "order", 1000L);
        
        assertNotEquals(dto, null);
        assertNotEquals(null, dto);
    }

    @Test
    void testEqualsWithDifferentClass() {
        PaymentApprovalRequestDto dto = new PaymentApprovalRequestDto("key", "order", 1000L);
        String differentObject = "not a dto";
        
        assertNotEquals(dto, differentObject);
    }
}