package com.nhnacademy.bookstoreorderapi.order.exception.internalservererror;

import com.nhnacademy.bookstoreorderapi.order.exception.OrderException;
import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends OrderException {
    
    public InternalServerErrorException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}