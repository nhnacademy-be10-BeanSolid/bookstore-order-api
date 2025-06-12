package com.nhnacademy.bookstoreorderapi.order.dto;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatusLog;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 주문 상태 변경 결과를 반환하는 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusChangeResponseDto implements ResponseDto {
    private Long orderId;          // 변경된 주문 ID
    private OrderStatus oldStatus; // 이전 상태
    private OrderStatus newStatus; // 변경된 새 상태
    private LocalDateTime changedAt; // 상태 변경 시각
    private Long changedBy;        // 상태 변경자 ID
    private String memo;           // 변경 메모

    public static StatusChangeResponseDto createFrom(OrderStatusLog log) {

        return StatusChangeResponseDto.builder()
                .orderId(log.getOrderId())
                .oldStatus(log.getOldStatus())
                .newStatus(log.getNewStatus())
                .changedAt(log.getChangedAt())
                .changedBy(log.getChangedBy())
                .memo(log.getMemo())
                .build();
    }
}