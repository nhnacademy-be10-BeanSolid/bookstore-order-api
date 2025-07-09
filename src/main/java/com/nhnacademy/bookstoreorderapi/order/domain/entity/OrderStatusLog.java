package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_status_logs")
@Getter
@NoArgsConstructor
public class OrderStatusLog extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus newStatus;

    @Column(nullable = false)
    private Long createdBy;

    @Lob
    private String memo;

    @ManyToOne
    private Order order;

    @Builder
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