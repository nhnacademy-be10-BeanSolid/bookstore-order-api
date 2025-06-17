package com.nhnacademy.bookstoreorderapi.payment.domain;

/**
 * 지원 결제 수단.
 * DB 의 method(VARCHAR) 컬럼과 1:1 매핑됩니다.
 */
public enum PaymentMethod {
    CARD,          // 신용/체크카드
    ACCOUNT,       // 계좌이체
    MOBILE,        // 휴대폰 소액결제
    POINT          // 포인트(사 내부)
}