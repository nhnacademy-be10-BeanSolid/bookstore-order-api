package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_returns")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderReturn extends BaseTimeEntity {

    public static final int RETURNS_FEE = 2_500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, updatable = false)
    private Order order;

    @Lob
    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    @Column(nullable = false)
    private Boolean damaged;

    @Column(nullable = false)
    private Boolean refundedAsPoints;

//    public static OrderReturn createFrom(Order order, ReturnRequest dto) {
//
//        return OrderReturn.builder()
//                .order(order)
//                .reason(dto.reason())
//                .damaged(dto.damaged())
//                .requestedAt(dto.requestedAt())
//                .build();
//    }
}
