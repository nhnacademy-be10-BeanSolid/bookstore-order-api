package com.nhnacademy.bookstoreorderapi.payment.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlreadyPaidExceptionTest {

    @Test
    void testConstructorWithOrderId() {
        String orderId = "order123";
        AlreadyPaidException exception = new AlreadyPaidException(orderId);

        assertEquals("이미 결제 완료된 주문입니다.", exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testConstructorWithNullOrderId() {
        AlreadyPaidException exception = new AlreadyPaidException(null);

        assertEquals("이미 결제 완료된 주문입니다.", exception.getMessage());
    }

    @Test
    void testConstructorWithEmptyOrderId() {
        AlreadyPaidException exception = new AlreadyPaidException("");

        assertEquals("이미 결제 완료된 주문입니다.", exception.getMessage());
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
        
        try {
            throw new AlreadyPaidException(orderId);
        } catch (AlreadyPaidException e) {
            assertEquals("이미 결제 완료된 주문입니다.", e.getMessage());
        }
    }
}