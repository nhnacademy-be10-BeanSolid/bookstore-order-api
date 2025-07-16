package com.nhnacademy.bookstoreorderapi.order.domain;

public enum OrderStatus {

    PENDING_PAY,  // 결제완료 후 배송대기
    SHIPPING, // 배송중
    COMPLETED,// 완료
    PENDING_RETURN, // 반품 대기
    RETURNED, // 반품 완료
    CANCELED;  // 결제취소
}