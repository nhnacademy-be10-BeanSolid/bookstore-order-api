package com.nhnacademy.bookstoreorderapi.order.dto.response;

import java.time.LocalDate;

// 주문 목록 조회용
public record OrderSummaryResponse(

    LocalDate orderDate,
    String orderNumber,
    String receiverName,
    Long totalPrice,
    String status
) {}
