package com.nhnacademy.bookstoreorderapi.order.exception.internalservererror;

public class ExternalServiceException extends InternalServerErrorException {
    
    public ExternalServiceException(String serviceName, String message) {
        super("내부 서비스 호출 오류 - " + serviceName + ": " + message);
    }
}