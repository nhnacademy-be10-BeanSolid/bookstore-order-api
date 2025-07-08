package com.nhnacademy.bookstoreorderapi.payment.exception;

//결제 정보 조회 중 오류 발생시 예외
public class PaymentInfoFetchException extends RuntimeException {
    public PaymentInfoFetchException(String message) {
        super(message);
    }
}
