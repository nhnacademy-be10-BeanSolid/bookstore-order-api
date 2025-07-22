package com.nhnacademy.bookstoreorderapi.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "주문 생성 요청 DTO")
public record CreateOrderRequest(

        @NotEmpty(message = "구매할 상품을 추가해주세요(현재: 구매할 상품 없음)")
        @Valid
        @Schema(description = "주문할 상품 목록", requiredMode = Schema.RequiredMode.REQUIRED)
        List<CreateOrderItemRequest> createItemRequests
) {
    @Schema(description = "주문 상품 정보")
    public record CreateOrderItemRequest(

            @NotNull @Positive
            @Schema(description = "도서 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            Long bookId,

            @NotNull @Positive
            @Schema(description = "주문 수량", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
            Integer quantity
    ) {}
}
