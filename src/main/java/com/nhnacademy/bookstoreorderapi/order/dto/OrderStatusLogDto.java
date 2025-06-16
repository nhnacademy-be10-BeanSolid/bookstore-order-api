package com.nhnacademy.bookstoreorderapi.order.dto;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatusLog;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusLogDto {
    private Long orderStateId;
    private Long orderId;
    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private LocalDateTime changedAt;
    private Long changedBy;
    private String memo;

    public static OrderStatusLogDto createFrom(OrderStatusLog log) {

        return OrderStatusLogDto.builder()
                .orderStateId(log.getOrderStateId())
                .orderId(log.getOrderId())
                .oldStatus(log.getOldStatus())
                .newStatus(log.getNewStatus())
                .changedAt(log.getChangedAt())
                .changedBy(log.getChangedBy())
                .memo(log.getMemo())
                .build();
    }
}