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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderReturn extends BaseCreatedAtEntity {

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
    private Boolean damaged;

    public OrderReturn(Order order, String reason, Boolean damaged) {
        this.order = order;
        this.reason = reason;
        this.damaged = damaged;
    }
}
