package com.nhnacademy.bookstoreorderapi.payment.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;     //PK, AUTO_INCREMENT

    @Column(name = "order_id", nullable = false)
    private Long orderId;       // 주문 ID (FK)

    @Column(name ="method" , nullable = false, length =  50)
    private String method;      // 결제 수단 (카드, 계좌)

    @Column(name ="provider", nullable = false, length = 50)
    private String provider;    //PG사

    @Column(name = "amount", nullable = false)
    private Long amount;        //결제 금액

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private String status;      //결제 상태

    @Column(name ="approved_at")
    private LocalDateTime approvedAt;   //결제 승인 시각

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;    //레코드 생성 시각

}
