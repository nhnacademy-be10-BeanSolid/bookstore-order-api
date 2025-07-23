package com.nhnacademy.bookstoreorderapi.payment.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlreadyPaidExceptionTest {

    @Test
    void testConstructorWithOrderId() {
        String orderId = "order123";
        AlreadyPaidException exception = new AlreadyPaidException("이미 결제 완료된 주문입니다: orderNumber=" + orderId);

        assertEquals("이미 결제 완료된 주문입니다: orderNumber=" + orderId, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testConstructorWithNullOrderId() {
        AlreadyPaidException exception = new AlreadyPaidException("이미 결제 완료된 주문입니다: orderNumber=null");

        assertEquals("이미 결제 완료된 주문입니다: orderNumber=null", exception.getMessage());
    }

    @Test
    void testConstructorWithEmptyOrderId() {
        AlreadyPaidException exception = new AlreadyPaidException("이미 결제 완료된 주문입니다: orderNumber=");

        assertEquals("이미 결제 완료된 주문입니다: orderNumber=", exception.getMessage());
    }

    @Test
    void testExceptionIsRuntimeException() {
        AlreadyPaidException exception = new AlreadyPaidException("order123");

        assertTrue(exception instanceof RuntimeException);
        assertFalse(exception instanceof Exception && !(exception instanceof RuntimeException));
    }

    @Test
    void testExceptionCanBeThrown() {
        assertThrows(AlreadyPaidException.class, () -> {
            throw new AlreadyPaidException("order123");
        });
    }

    @Test
    void testExceptionMessage() {
        String orderId = "test-order-id";
        String expectedMessage = "이미 결제 완료된 주문입니다: orderNumber=" + orderId;
        
        try {
            throw new AlreadyPaidException(expectedMessage);
        } catch (AlreadyPaidException e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }
}