package com.nhnacademy.bookstoreorderapi.order.dto.response;

public record UserOrderAmountResponse(
    Long userNo,
    Long pureOrderAmount
) {}
