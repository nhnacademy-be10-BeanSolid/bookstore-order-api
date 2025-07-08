package com.nhnacademy.bookstoreorderapi.payment.exception;

public class AlreadyPaidException extends RuntimeException {
    public AlreadyPaidException(String orderId) {
        super("이미 결제 완료된 주문입니다.");
    }
}
