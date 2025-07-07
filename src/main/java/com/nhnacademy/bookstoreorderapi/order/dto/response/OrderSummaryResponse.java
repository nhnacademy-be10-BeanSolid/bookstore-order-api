package com.nhnacademy.bookstoreorderapi.order.dto.response;

import java.time.LocalDate;

// 회원 주문 목록 조회용
public record OrderSummaryResponse(
    LocalDate orderDate,
    String orderId,
    String receiverName,
    Long totalPrice
) {}
