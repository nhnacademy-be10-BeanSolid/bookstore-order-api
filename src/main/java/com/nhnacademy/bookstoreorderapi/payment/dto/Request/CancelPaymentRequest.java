package com.nhnacademy.bookstoreorderapi.payment.dto.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Schema(description = "결제 취소 요청 DTO")
public class CancelPaymentRequest {
    
    @NotBlank
    @Schema(description = "주문 ID", example = "ORDER-20240101-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderId;

    @Min(1)
    @Schema(description = "취소할 금액", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED)
    private long amount;

    @NotBlank
    @Schema(description = "취소 사유", example = "고객 요청", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cancelReason;
}