// src/test/java/com/nhnacademy/bookstoreorderapi/order/domain/entity/OrderStatusLogTest.java
package com.nhnacademy.bookstoreorderapi.order.entity;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatusLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusLogTest {

    @Test
    @DisplayName("Builder로 생성 시 모든 필드가 정확히 세팅된다")
    void builderAssignsAllFields() {
        LocalDateTime now = LocalDateTime.now();

        OrderStatusLog log = OrderStatusLog.builder()
                .orderStateId(10L)
                .orderId(5L)
                .oldStatus(OrderStatus.PENDING)
                .newStatus(OrderStatus.SHIPPING)
                .changedAt(now)
                .changedBy(999L)
                .memo("테스트 메모")
                .build();

        assertThat(log.getOrderStateId()).isEqualTo(10L);
        assertThat(log.getOrderId()).isEqualTo(5L);
        assertThat(log.getOldStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(log.getNewStatus()).isEqualTo(OrderStatus.SHIPPING);
        assertThat(log.getChangedAt()).isSameAs(now);
        assertThat(log.getChangedBy()).isEqualTo(999L);
        assertThat(log.getMemo()).isEqualTo("테스트 메모");
    }

    @Test
    @DisplayName("Setter와 Getter가 올바르게 동작한다")
    void setterAndGetterWork() {
        OrderStatusLog log = new OrderStatusLog();
        LocalDateTime ts = LocalDateTime.of(2025, 6, 11, 12, 0);

        log.setOrderStateId(20L);
        log.setOrderId(8L);
        log.setOldStatus(OrderStatus.SHIPPING);
        log.setNewStatus(OrderStatus.COMPLETED);
        log.setChangedAt(ts);
        log.setChangedBy(123L);
        log.setMemo("완료 처리");

        assertThat(log.getOrderStateId()).isEqualTo(20L);
        assertThat(log.getOrderId()).isEqualTo(8L);
        assertThat(log.getOldStatus()).isEqualTo(OrderStatus.SHIPPING);
        assertThat(log.getNewStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(log.getChangedAt()).isEqualTo(ts);
        assertThat(log.getChangedBy()).isEqualTo(123L);
        assertThat(log.getMemo()).isEqualTo("완료 처리");
    }
}