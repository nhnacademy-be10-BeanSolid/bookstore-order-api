package com.nhnacademy.bookstoreorderapi.order.exception.unauthorized;

public class NotMemberException extends UnauthorizedException {
    public NotMemberException(String message) {
        super(message);
    }
}
