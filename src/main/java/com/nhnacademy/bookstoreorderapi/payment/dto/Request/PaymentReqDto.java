// src/main/java/com/nhnacademy/bookstoreorderapi/payment/dto/Request/PaymentReqDto.java
package com.nhnacademy.bookstoreorderapi.payment.dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReqDto {
   @Min(value = 1000, message = "최소 결제 금액은 1,000원입니다.")
   @JsonProperty("payAmount")
   private Long payAmount;

   @NotBlank
   private String payType;

   @NotBlank
   private String orderName;

   // 클라이언트에서 커스터마이징한 URL (옵션)
   private String successUrl;
   private String failUrl;

   public Payment toEntity(long orderId) {
      return Payment.builder()
              .orderId(orderId)
              .payAmount(this.payAmount)
              .payType(this.payType)
              .payName(this.orderName)
              .build();
   }
}