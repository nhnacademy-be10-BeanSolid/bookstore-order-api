// src/main/java/com/nhnacademy/bookstoreorderapi/payment/dto/Response/PaymentResDto.java
package com.nhnacademy.bookstoreorderapi.payment.dto.Response;

import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class PaymentResDto {

    private Long paymentId;        // DB PK
    private String orderId;        // 연결된 주문의 비즈니스 ID

    private Long payAmount;        // 결제 금액
    private String payType;        // 결제 수단
    private String payName;        // 주문명

    private String paymentKey;     // Toss API 가 내려준 키


    private String successUrl;     // 콜백 URL
    private String failUrl;
}