package com.nhnacademy.bookstoreorderapi.payment.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.domain.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    private Payment payment;
    private Order order;

    @BeforeEach
    void setUp() throws Exception {
        // Using reflection to bypass protected constructor
        java.lang.reflect.Constructor<Order> constructor = Order.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        order = constructor.newInstance();
        payment = new Payment();
        payment.setPaymentId(1L);
        payment.setOrder(order);
        payment.setPaymentKey("testPaymentKey");
        payment.setPayType(PayType.CARD);
        payment.setPayAmount(1000L);
        payment.setPayName("Test Payment");
        payment.setPaymentStatus(PaymentStatus.PENDING);
    }

    @Test
    void testPaymentCreation() {
        assertNotNull(payment);
        assertEquals(1L, payment.getPaymentId());
        assertEquals(order, payment.getOrder());
        assertEquals("testPaymentKey", payment.getPaymentKey());
        assertEquals(PayType.CARD, payment.getPayType());
        assertEquals(1000L, payment.getPayAmount());
        assertEquals("Test Payment", payment.getPayName());
        assertEquals(PaymentStatus.PENDING, payment.getPaymentStatus());
    }

    @Test
    void testSetPaymentStatus() {
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        assertEquals(PaymentStatus.SUCCESS, payment.getPaymentStatus());
    }
}
