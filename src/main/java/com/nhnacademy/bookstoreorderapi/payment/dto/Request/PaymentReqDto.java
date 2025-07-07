package com.nhnacademy.bookstoreorderapi.payment.dto.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReqDto {

   @NotNull(message = "결제 금액(payAmount)는 필수입니다.")
   private Long payAmount;

   @NotNull(message = "결제 수단(payType)는 필수입니다.")
   @JsonFormat(shape = JsonFormat.Shape.STRING)
   private PayType payType;

   @NotNull(message = "주문명(payName)은 필수입니다.")
   private String payName;

   private String successUrl;
   private String failUrl;
}