package com.nhnacademy.bookstoreorderapi.payment.exception;

public class NonMemberPointUsageAttemptException extends RuntimeException {
    public NonMemberPointUsageAttemptException(String message) {
        super(message);
    }
}
