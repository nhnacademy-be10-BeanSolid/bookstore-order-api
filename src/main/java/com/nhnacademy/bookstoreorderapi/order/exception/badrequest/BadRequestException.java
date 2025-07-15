package com.nhnacademy.bookstoreorderapi.order.exception.badrequest;

import com.nhnacademy.bookstoreorderapi.order.exception.OrderException;
import org.springframework.http.HttpStatus;

public class BadRequestException extends OrderException {
    
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}