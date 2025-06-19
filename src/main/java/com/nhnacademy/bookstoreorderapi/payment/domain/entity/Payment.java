package com.nhnacademy.bookstoreorderapi.payment.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    /** === DB 컬럼 === */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;                 // PK (결제 ID)

//    @Column(name = "order_id", nullable = false)
//    private Long orderId;                   // 주문 ID(FK)
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "pay_type", nullable = false, length = 50)
    private String payType;                 // 결제수단

    @Column(name = "pay_amount", nullable = false)
    private Long payAmount;                 // 결제금액

    @Column(name = "pay_name", nullable = false, length = 50)
    private String payName;                 // 주문/결제 제목

    @Column(name = "pay_success_yn")
    private Boolean paySuccessYn;           // 결제 성공 여부

    @Column(name = "pay_fail_reason")
    private String payFailReason;           // 결제 실패 사유

    @Column(name = "payment_key")
    private String paymentKey;              // 토스 결제키
}