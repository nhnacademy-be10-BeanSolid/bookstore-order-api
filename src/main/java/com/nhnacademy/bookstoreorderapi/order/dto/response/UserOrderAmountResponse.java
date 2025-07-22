package com.nhnacademy.bookstoreorderapi.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자별 주문 금액 응답 DTO")
public record UserOrderAmountResponse(
    
    @Schema(description = "사용자 번호", example = "12345")
    Long userNo,
    
    @Schema(description = "순수 주문 금액", example = "150000")
    Long pureOrderAmount
) {}
