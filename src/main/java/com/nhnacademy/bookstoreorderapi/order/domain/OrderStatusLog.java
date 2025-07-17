package com.nhnacademy.bookstoreorderapi.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_status_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderStatusLog extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private OrderStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private OrderStatus newStatus;

    @Column(nullable = false, updatable = false)
    private Long createdBy;

    @Lob
    @Column(updatable = false)
    private String memo;

    public OrderStatusLog (OrderStatus oldStatus,
                           OrderStatus newStatus,
                           Long userNo,
                           String memo,
                           Order order) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.createdBy = userNo;
        this.memo = memo;
        this.order = order;
    }
}