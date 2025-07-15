package com.nhnacademy.bookstoreorderapi.order.exception.unauthorized;

public class NotMemberException extends RuntimeException {
    public NotMemberException(String message) {
        super(message);
    }
}
