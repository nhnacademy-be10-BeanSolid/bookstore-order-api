package com.nhnacademy.bookstoreorderapi.payment.dto.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "결제 요청 DTO")
public class PaymentReqDto {

   @Schema(description = "주문 ID", example = "202507-abcdef-123456")
   private String orderId;

   @NotNull(message = "결제 금액(payAmount)는 필수입니다.")
   @Schema(description = "결제 금액", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED)
   private Long payAmount;

   @NotNull(message = "결제 수단(payType)는 필수입니다.")
   @JsonFormat(shape = JsonFormat.Shape.STRING)
   @Schema(description = "결제 수단", example = "CARD", requiredMode = Schema.RequiredMode.REQUIRED)
   private PayType payType;

   @NotNull(message = "주문명(payName)은 필수입니다.")
   @Schema(description = "주문명", example = "도서 주문", requiredMode = Schema.RequiredMode.REQUIRED)
   private String payName;

   @Schema(description = "성공 콜백 URL", example = "https://example.com/success")
   private String successUrl;
   
   @Schema(description = "실패 콜백 URL", example = "https://example.com/fail")
   private String failUrl;
   
   @Schema(description = "사용한 포인트", example = "1000")
   private int usedPoint;
}