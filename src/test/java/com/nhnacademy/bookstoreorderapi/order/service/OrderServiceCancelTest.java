// src/test/java/com/nhnacademy/bookstoreorderapi/order/service/OrderServiceCancelTest.java
package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.CanceledOrder;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.repository.CanceledOrderRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceCancelTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CanceledOrderRepository canceledOrderRepository;

    @InjectMocks
    private OrderService orderService;

    private Order pendingOrder;
    private Order shippedOrder;

    @BeforeEach
    void setUp() {
        pendingOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.PENDING)
                .build();

        shippedOrder = Order.builder()
                .id(2L)
                .status(OrderStatus.SHIPPING)
                .build();
    }

    @Test
    void cancelOrder_whenPending_savesCanceledRecordAndUpdatesStatus() {
        // 준비
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        ArgumentCaptor<CanceledOrder> captor = ArgumentCaptor.forClass(CanceledOrder.class);

        // 실행
        orderService.cancelOrder(1L, "고객 요청");

        // 검증: 주문 상태 변경
        assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
        // 검증: 취소 기록 저장
        verify(canceledOrderRepository).save(captor.capture());
        CanceledOrder record = captor.getValue();
        assertThat(record.getOrderId()).isEqualTo(1L);
        assertThat(record.getReason()).isEqualTo("고객 요청");
        assertThat(record.getCanceledAt()).isBeforeOrEqualTo(LocalDateTime.now());
        // 검증: 주문 저장 호출
        verify(orderRepository).save(pendingOrder);
    }

    @Test
    void cancelOrder_whenNotPending_throwsException() {
        // 준비: SHIPPING 상태인 주문
        when(orderRepository.findById(2L)).thenReturn(Optional.of(shippedOrder));

        // 실행 & 검증
        assertThatThrownBy(() -> orderService.cancelOrder(2L, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("배송 전 주문만 취소 가능합니다.");
        // 취소 기록 저장 안 함
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_whenNotFound_throwsNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(99L, "테스트"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("주문을 찾을 수 없습니다.");
    }
}