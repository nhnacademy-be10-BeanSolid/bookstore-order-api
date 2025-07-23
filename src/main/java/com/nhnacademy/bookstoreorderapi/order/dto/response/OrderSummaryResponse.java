package com.nhnacademy.bookstoreorderapi.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "주문 목록 조회용 응답 DTO")
public record OrderSummaryResponse(

    @Schema(description = "주문 날짜", example = "2024-01-01")
    LocalDate orderDate,
    
    @Schema(description = "주문 번호", example = "ORDER-20240101-001")
    String orderNumber,
    
    @Schema(description = "받는 사람 이름", example = "홍길동")
    String receiverName,
    
    @Schema(description = "총 주문 금액", example = "50000")
    Long totalPrice,
    
    @Schema(description = "주문 상태", example = "PENDING")
    String status
) {}
