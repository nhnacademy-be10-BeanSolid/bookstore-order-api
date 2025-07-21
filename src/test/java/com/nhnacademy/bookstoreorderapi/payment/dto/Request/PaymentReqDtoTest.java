package com.nhnacademy.bookstoreorderapi.payment.dto.Request;

import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentReqDtoTest {

    @Test
    void testConstructorAndGetters() {
        PaymentReqDto dto = new PaymentReqDto();
        dto.setOrderId("order123");
        dto.setPayAmount(10000L);
        dto.setPayType(PayType.CARD);
        dto.setPayName("Test Payment");
        dto.setSuccessUrl("http://success.com");
        dto.setFailUrl("http://fail.com");
        dto.setUsedPoint(100);
        
        assertEquals("order123", dto.getOrderId());
        assertEquals(10000L, dto.getPayAmount());
        assertEquals(PayType.CARD, dto.getPayType());
        assertEquals("Test Payment", dto.getPayName());
        assertEquals("http://success.com", dto.getSuccessUrl());
        assertEquals("http://fail.com", dto.getFailUrl());
        assertEquals(100, dto.getUsedPoint());
    }

    @Test
    void testEqualsAndHashCode() {
        PaymentReqDto dto1 = new PaymentReqDto();
        dto1.setOrderId("order1");
        dto1.setPayAmount(1000L);
        dto1.setPayType(PayType.CARD);
        dto1.setPayName("Payment 1");
        
        PaymentReqDto dto2 = new PaymentReqDto();
        dto2.setOrderId("order1");
        dto2.setPayAmount(1000L);
        dto2.setPayType(PayType.CARD);
        dto2.setPayName("Payment 1");
        
        PaymentReqDto dto3 = new PaymentReqDto();
        dto3.setOrderId("order2");
        dto3.setPayAmount(2000L);
        dto3.setPayType(PayType.ACCOUNT);
        dto3.setPayName("Payment 2");
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testToString() {
        PaymentReqDto dto = new PaymentReqDto();
        dto.setOrderId("order123");
        dto.setPayAmount(5000L);
        dto.setPayType(PayType.CARD);
        
        String toString = dto.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("order123"));
        assertTrue(toString.contains("5000"));
        assertTrue(toString.contains("CARD"));
    }

    @Test
    void testEqualsWithNull() {
        PaymentReqDto dto = new PaymentReqDto();
        dto.setOrderId("order");
        
        assertNotEquals(null, dto);
        assertNotEquals(null, dto);
    }

    @Test
    void testEqualsWithDifferentClass() {
        PaymentReqDto dto = new PaymentReqDto();
        String differentObject = "not a dto";
        
        assertNotEquals(dto, differentObject);
    }
}