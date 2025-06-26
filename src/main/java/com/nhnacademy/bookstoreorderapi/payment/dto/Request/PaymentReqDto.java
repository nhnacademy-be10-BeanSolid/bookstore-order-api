package com.nhnacademy.bookstoreorderapi.payment.dto.Request;

import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentReqDto {

   @NotNull(message = "결제 금액(payAmount)는 필수입니다.")
   private Long payAmount;

   @NotNull(message = "결제 수단(payType)는 필수입니다.")
   private PayType payType;

   @NotNull(message = "주문명(payName)은 필수입니다.")
   private String payName;

   private String successUrl;
   private String failUrl;
}