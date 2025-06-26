package com.nhnacademy.bookstoreorderapi.payment.dto.Response;

import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class PaymentResDto {

    private Long paymentId;        // DB PK
    private String orderId;        // 연결된 주문의 비즈니스 ID
    private String payType;        // 결제 수단

    private Long payAmount;        // 결제 금액
    private String payName;        // 주문 and 결제 제목
    private String paymentStatus; // 결제 상태
    private String paymentKey;     // Toss API 가 내려준 키



    private String successUrl;     // 콜백 URL
    private String failUrl;
    private String redirectUrl;  //


}