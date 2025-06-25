// src/main/java/com/nhnacademy/bookstoreorderapi/payment/domain/entity/Payment.java
package com.nhnacademy.bookstoreorderapi.payment.domain.entity;

import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    /* PK */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    /* 총 결제 금액 (Long) */
    @Column(nullable = false)
    private Long payAmount;

    /* 결제 수단 (CARD, ACCOUNT …) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayType payType;

    /* “도서 4권” 등 */
    @Column(nullable = false, length = 100)
    private String payName;

    /* ───── FK: 주문 번호 (varchar 64) ───── */
    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    /* 결제 성공 여부 및 부가정보 */
    @Column(name = "pay_success_yn")
    private Boolean paySuccessYn;

    @Column(length = 255)
    private String paymentKey;

    @Column(length = 255)
    private String payFailReason;
}