package com.nhnacademy.bookstoreorderapi.payment.domain;

public enum PaymentStatus {

    PENDING,    //결제 요청만 들어온 상태 (승인 전)
    APPROVED,   // PG사로부터 승인을 받은 상태.
    FAILED,     //결제가 거절된 상태
//    CANCELED,   //결제 취소된 상태
//    REFUNDED    //결제 금액이 전액 환불된 상태
}
