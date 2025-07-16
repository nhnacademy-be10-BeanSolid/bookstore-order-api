package com.nhnacademy.bookstoreorderapi.order.domain.entity;

public enum OrderStatus {

    PENDING,  // 결제완료 후 배송대기
    SHIPPING, // 배송중
    COMPLETED,// 완료
    RETURNED, // 반품
    CANCELED;  // 결제취소
}
