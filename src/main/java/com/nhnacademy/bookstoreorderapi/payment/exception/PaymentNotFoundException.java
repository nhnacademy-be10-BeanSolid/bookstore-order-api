package com.nhnacademy.bookstoreorderapi.payment.exception;

//PaymentKey 조회되는 결제 레코드가 없을 때 예외처리
public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String PaymentKey) {
        super("결제 내역을 찾을 수 없습니다: paymentKey=" + PaymentKey);
    }
}
