package com.nhnacademy.bookstoreorderapi.payment.dto.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "결제 승인 요청 DTO")
public class PaymentApprovalRequestDto {

    @Schema(description = "토스 결제 키", example = "toss-payment-key-123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String paymentKey;
    
    @Schema(description = "주문 ID", example = "202507-abcdef-123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderId;
    
    @Schema(description = "결제 금액", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED)
    private long amount;
}
