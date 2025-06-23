// src/main/java/com/nhnacademy/bookstoreorderapi/payment/domain/entity/Payment.java
package com.nhnacademy.bookstoreorderapi.payment.domain.entity;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(nullable = false)
    private Long payAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayType payType;

    @Column(nullable = false, length = 100)
    private String payName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            referencedColumnName = "order_id",
            nullable = false
    )
    private Order order;

    @Column(name = "pay_success_yn")
    private Boolean paySuccessYn;

    @Column(length = 255)
    private String paymentKey;

    @Column(length = 255)
    private String payFailReason;
}