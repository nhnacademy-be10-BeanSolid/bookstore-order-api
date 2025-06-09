package com.nhnacademy.bookstoreorderapi.order.domain.entity;



public enum OrderStatus {
    PENDING,  // 대기
    SHIPPING, // 배송중
    COMPLETED,// 완료
    RETURNED, // 반품
    CANCELED  // 취소
}