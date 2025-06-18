package com.nhnacademy.bookstoreorderapi.payment.dto.Response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResDto {

    private Long paymentId;   // (옵션) DB PK
//    private Long orderId;     // 주문 ID
    private String orderId;

    private Long payAmount;   // ★ 컨트롤러에서 쓰는 exact 이름·타입

    private String payType;   // 카드 / 계좌 등
    private String orderName; // 주문/결제 제목

    private String successUrl;
    private String failUrl;
}