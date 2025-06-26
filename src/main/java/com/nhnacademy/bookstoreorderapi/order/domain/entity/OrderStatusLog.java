package com.nhnacademy.bookstoreorderapi.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderStatusLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_state_id")
    private Long orderStateId;  //주문 상태 로그 ID

    @Column(name = "order_id", nullable = false)
    private Long orderId;       // 주문 ID

    @Enumerated(EnumType.STRING)
    @Column(name = "oldStatus", nullable = false, length = 20)
    private OrderStatus oldStatus;  //이전 상태

    @Enumerated(EnumType.STRING)
    @Column(name = "newStatus", nullable = false, length = 20)
    private OrderStatus newStatus;  //변경된 상태

    @Column(name = "changedAt", nullable = false)
    private LocalDateTime changedAt;    //변경 시작

    @Column(name = "changedBy")
    private Long changedBy;    // 변경자 ID(관리자 등)

//    @Column(name = "memo", columnDefinition = "TEXT")
    @Lob
    private String memo;       // 변경 메모

    public static OrderStatusLog createFrom(Long orderId, OrderStatus oldStatus,
                                            OrderStatus newStatus, Long changedBy, String memo) {

        return OrderStatusLog.builder()
                .orderId(orderId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedAt(LocalDateTime.now())
                .changedBy(changedBy)
                .memo(memo)
                .build();
    }
}