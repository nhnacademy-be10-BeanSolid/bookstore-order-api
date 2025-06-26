package com.nhnacademy.bookstoreorderapi.payment.domain;


public enum PaymentStatus {
    SUCCESS,    //결제 완료
    FAIL,       // 결제 실패(에러·거절 등)
    CANCEL,     //정산 완료 후 사용자가 취소
    PENDING     // 필요 없다면 제거
}