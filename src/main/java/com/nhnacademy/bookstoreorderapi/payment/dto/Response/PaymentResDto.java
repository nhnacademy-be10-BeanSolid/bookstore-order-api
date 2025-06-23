// src/main/java/com/nhnacademy/bookstoreorderapi/payment/dto/Response/PaymentResDto.java
package com.nhnacademy.bookstoreorderapi.payment.dto.Response;

import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResDto {
    private Long    paymentId;
    private String  orderId;
    private Long    payAmount;
    private String  payType;
    private String  orderName;

    // 토스페이 결제 요청 응답에서 돌아오는 키
    private String  paymentKey;
    private String  nextRedirectAppUrl;
    private String  nextRedirectPcUrl;

    // 성공/실패 리다이렉트용 (필요하다면)
    private String  successUrl;
    private String  failUrl;
}