package com.nhnacademy.bookstoreorderapi.payment.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.domain.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    //pk
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    //FK: 주문 번호
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 결제 수단
    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", nullable = false, length = 50)
    private PayType payType;

    //결제 금액
    @Column(name = "pay_amount", nullable = false)
    private long payAmount;


    //주문 결제 제목  */
    @Column(name = "pay_name", nullable = false, length = 50)
    private String payName;

    //결제 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private PaymentStatus paymentStatus;


    //Toss 결제 키
    @Column(name = "payment_key", nullable = false, length = 200)
    private String paymentKey;



    // 기본값 “도서 구매” 자동 입력
    @PrePersist
    private void setDefaultPayName() {
        if (this.payName == null || this.payName.isBlank()) {
            this.payName = "도서 구매";
        }
    }
}
