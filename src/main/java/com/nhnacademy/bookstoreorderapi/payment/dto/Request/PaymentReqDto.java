package com.nhnacademy.bookstoreorderapi.payment.dto.Request;

import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentReqDto {

   private PayType payType;
   private Long amount;
   private String orderName;

   public Payment toEntity(Long orderId){
       return Payment.builder()
               .orderId(orderId)
               .payType(String.valueOf(payType))
               .payAmount(amount)
               .payName(orderName)
               .paySuccessYn(false)
               .build();
   }
}

