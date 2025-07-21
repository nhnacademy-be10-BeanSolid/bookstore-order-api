package com.nhnacademy.bookstoreorderapi.payment.dto.Response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentResDtoTest {

    @Test
    void testBuilderAndGetters() {
        PaymentResDto dto = PaymentResDto.builder()
                .paymentId(1L)
                .orderId("order123")
                .payType("CARD")
                .payAmount(10000L)
                .payName("Test Payment")
                .paymentStatus("COMPLETED")
                .paymentKey("payment_key_123")
                .successUrl("https://success.com")
                .failUrl("https://fail.com")
                .redirectUrl("https://redirect.com")
                .build();

        assertEquals(1L, dto.getPaymentId());
        assertEquals("order123", dto.getOrderId());
        assertEquals("CARD", dto.getPayType());
        assertEquals(10000L, dto.getPayAmount());
        assertEquals("Test Payment", dto.getPayName());
        assertEquals("COMPLETED", dto.getPaymentStatus());
        assertEquals("payment_key_123", dto.getPaymentKey());
        assertEquals("https://success.com", dto.getSuccessUrl());
        assertEquals("https://fail.com", dto.getFailUrl());
        assertEquals("https://redirect.com", dto.getRedirectUrl());
    }

    @Test
    void testAllArgsConstructor() {
        PaymentResDto dto = new PaymentResDto(
                1L, "order123", "CARD", 10000L, "Test Payment",
                "COMPLETED", "payment_key_123", "https://success.com",
                "https://fail.com", "https://redirect.com"
        );

        assertEquals(1L, dto.getPaymentId());
        assertEquals("order123", dto.getOrderId());
        assertEquals("CARD", dto.getPayType());
        assertEquals(10000L, dto.getPayAmount());
        assertEquals("Test Payment", dto.getPayName());
        assertEquals("COMPLETED", dto.getPaymentStatus());
        assertEquals("payment_key_123", dto.getPaymentKey());
        assertEquals("https://success.com", dto.getSuccessUrl());
        assertEquals("https://fail.com", dto.getFailUrl());
        assertEquals("https://redirect.com", dto.getRedirectUrl());
    }

    @Test
    void testNoArgsConstructor() {
        PaymentResDto dto = new PaymentResDto();
        assertNotNull(dto);
    }

    @Test
    void testSetters() {
        PaymentResDto dto = new PaymentResDto();
        dto.setPaymentId(2L);
        dto.setOrderId("order456");
        dto.setPayType("ACCOUNT");
        dto.setPayAmount(20000L);
        dto.setPayName("Another Payment");
        dto.setPaymentStatus("PENDING");
        dto.setPaymentKey("payment_key_456");
        dto.setSuccessUrl("https://success2.com");
        dto.setFailUrl("https://fail2.com");
        dto.setRedirectUrl("https://redirect2.com");

        assertEquals(2L, dto.getPaymentId());
        assertEquals("order456", dto.getOrderId());
        assertEquals("ACCOUNT", dto.getPayType());
        assertEquals(20000L, dto.getPayAmount());
        assertEquals("Another Payment", dto.getPayName());
        assertEquals("PENDING", dto.getPaymentStatus());
        assertEquals("payment_key_456", dto.getPaymentKey());
        assertEquals("https://success2.com", dto.getSuccessUrl());
        assertEquals("https://fail2.com", dto.getFailUrl());
        assertEquals("https://redirect2.com", dto.getRedirectUrl());
    }

    @Test
    void testEqualsAndHashCode() {
        PaymentResDto dto1 = PaymentResDto.builder()
                .paymentId(1L)
                .orderId("order1")
                .payType("CARD")
                .payAmount(1000L)
                .payName("Payment 1")
                .paymentStatus("COMPLETED")
                .paymentKey("key1")
                .build();

        PaymentResDto dto2 = PaymentResDto.builder()
                .paymentId(1L)
                .orderId("order1")
                .payType("CARD")
                .payAmount(1000L)
                .payName("Payment 1")
                .paymentStatus("COMPLETED")
                .paymentKey("key1")
                .build();

        PaymentResDto dto3 = PaymentResDto.builder()
                .paymentId(2L)
                .orderId("order2")
                .payType("ACCOUNT")
                .payAmount(2000L)
                .payName("Payment 2")
                .paymentStatus("PENDING")
                .paymentKey("key2")
                .build();

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        PaymentResDto dto = PaymentResDto.builder()
                .paymentId(1L)
                .orderId("order123")
                .payType("CARD")
                .payAmount(5000L)
                .build();

        String toString = dto.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("order123"));
        assertTrue(toString.contains("CARD"));
        assertTrue(toString.contains("5000"));
    }

    @Test
    void testEqualsWithNull() {
        PaymentResDto dto = new PaymentResDto();
        dto.setPaymentId(1L);

        assertNotEquals(dto, null);
    }

    @Test
    void testEqualsWithDifferentClass() {
        PaymentResDto dto = new PaymentResDto();
        String differentObject = "not a dto";

        assertNotEquals(dto, differentObject);
    }
}