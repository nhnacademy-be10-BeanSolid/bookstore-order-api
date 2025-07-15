package com.nhnacademy.bookstoreorderapi.order.exception.unauthorized;

import com.nhnacademy.bookstoreorderapi.order.exception.OrderException;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends OrderException {
    
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}