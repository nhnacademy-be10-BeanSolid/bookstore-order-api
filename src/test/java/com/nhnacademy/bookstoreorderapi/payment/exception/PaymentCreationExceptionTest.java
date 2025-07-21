package com.nhnacademy.bookstoreorderapi.payment.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentCreationExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "결제 생성에 실패했습니다.";
        PaymentCreationException exception = new PaymentCreationException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testConstructorWithNullMessage() {
        PaymentCreationException exception = new PaymentCreationException(null);

        assertNull(exception.getMessage());
    }

    @Test
    void testConstructorWithEmptyMessage() {
        String emptyMessage = "";
        PaymentCreationException exception = new PaymentCreationException(emptyMessage);

        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    void testExceptionIsRuntimeException() {
        PaymentCreationException exception = new PaymentCreationException("test message");

        assertTrue(exception instanceof RuntimeException);
        assertFalse(exception instanceof Exception && !(exception instanceof RuntimeException));
    }

    @Test
    void testExceptionCanBeThrown() {
        String errorMessage = "결제 정보 생성 중 오류 발생";
        
        assertThrows(PaymentCreationException.class, () -> {
            throw new PaymentCreationException(errorMessage);
        });
    }

    @Test
    void testExceptionMessagePreservation() {
        String originalMessage = "API 호출 실패로 결제 생성 불가";
        
        try {
            throw new PaymentCreationException(originalMessage);
        } catch (PaymentCreationException e) {
            assertEquals(originalMessage, e.getMessage());
        }
    }

    @Test
    void testMultipleExceptionInstances() {
        PaymentCreationException exception1 = new PaymentCreationException("첫 번째 오류");
        PaymentCreationException exception2 = new PaymentCreationException("두 번째 오류");

        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertEquals("첫 번째 오류", exception1.getMessage());
        assertEquals("두 번째 오류", exception2.getMessage());
    }
}