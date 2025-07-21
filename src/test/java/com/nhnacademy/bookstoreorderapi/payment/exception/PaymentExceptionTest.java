package com.nhnacademy.bookstoreorderapi.payment.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentExceptionTest {

    @Test
    void testPaymentNotFoundException() {
        String message = "결제 정보를 찾을 수 없습니다.";
        PaymentNotFoundException exception = new PaymentNotFoundException(message);

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testPaymentConfirmationException() {
        String message = "결제 승인에 실패했습니다.";
        PaymentConfirmationException exception = new PaymentConfirmationException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testPaymentCancellationException() {
        String message = "결제 취소에 실패했습니다.";
        PaymentCancellationException exception = new PaymentCancellationException(message);

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testPaymentInfoFetchException() {
        String message = "결제 정보 조회에 실패했습니다.";
        PaymentInfoFetchException exception = new PaymentInfoFetchException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testNonMemberPointUsageAttemptException() {
        String message = "비회원은 포인트를 사용할 수 없습니다.";
        NonMemberPointUsageAttemptException exception = new NonMemberPointUsageAttemptException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testNotEnoughPointException() {
        String message = "포인트가 부족합니다.";
        NotEnoughPointException exception = new NotEnoughPointException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testRedirectUrlNotFoundException() {
        String message = "리다이렉트 URL을 찾을 수 없습니다.";
        RedirectUrlNotFoundException exception = new RedirectUrlNotFoundException(message);

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testAllExceptionsCanBeThrown() {
        assertThrows(PaymentNotFoundException.class, () -> {
            throw new PaymentNotFoundException("test");
        });

        assertThrows(PaymentConfirmationException.class, () -> {
            throw new PaymentConfirmationException("test");
        });

        assertThrows(PaymentCancellationException.class, () -> {
            throw new PaymentCancellationException("test");
        });

        assertThrows(PaymentInfoFetchException.class, () -> {
            throw new PaymentInfoFetchException("test");
        });

        assertThrows(NonMemberPointUsageAttemptException.class, () -> {
            throw new NonMemberPointUsageAttemptException("test");
        });

        assertThrows(NotEnoughPointException.class, () -> {
            throw new NotEnoughPointException("test");
        });

        assertThrows(RedirectUrlNotFoundException.class, () -> {
            throw new RedirectUrlNotFoundException("test");
        });
    }

    @Test
    void testExceptionHierarchy() {
        PaymentNotFoundException exception = new PaymentNotFoundException("test");
        
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }
}