package com.nhnacademy.bookstoreorderapi.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문 상태 변경 요청 DTO")
public record OrderStatusRequest(

        @NotNull
        @Schema(description = "주문 액션", example = "CANCEL", requiredMode = Schema.RequiredMode.REQUIRED)
        OrderAction action,

        @NotBlank
        @Schema(description = "변경 사유", example = "고객 요청으로 인한 취소", requiredMode = Schema.RequiredMode.REQUIRED)
        String reason,

        @Schema(description = "상품 손상 여부", example = "false")
        Boolean damaged
) {
    @Schema(description = "주문 액션 유형")
    public enum OrderAction {
        @Schema(description = "주문 취소")
        CANCEL,
        @Schema(description = "주문 반품")
        RETURN
    }
}