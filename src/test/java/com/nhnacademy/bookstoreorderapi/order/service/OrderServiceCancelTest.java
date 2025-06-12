// src/test/java/com/nhnacademy/bookstoreorderapi/order/service/OrderServiceCancelTest.java
package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.CanceledOrder;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.InvalidOrderStatusChangeException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.ResourceNotFoundException;
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
    private Order shippingOrder;

    @BeforeEach
    void setUp() {
        pendingOrder = Order.builder()
                .id(1L)
                .status(OrderStatus.PENDING)
                .build();

        shippingOrder = Order.builder()
                .id(2L)
                .status(OrderStatus.SHIPPING)
                .build();
    }

    @Test
    void cancelOrder_whenPending_savesCanceledRecordAndUpdatesStatus() {
        // given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        ArgumentCaptor<CanceledOrder> captor = ArgumentCaptor.forClass(CanceledOrder.class);

        // when
        orderService.cancelOrder(1L, "고객 요청");

        // then: 주문 상태가 CANCELED 로 변경
        assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);

        // then: CanceledOrderRepository.save() 호출 및 필드 검증
        verify(canceledOrderRepository).save(captor.capture());
        CanceledOrder record = captor.getValue();
        assertThat(record.getOrderId()).isEqualTo(1L);
        assertThat(record.getReason()).isEqualTo("고객 요청");
        assertThat(record.getCanceledAt()).isBeforeOrEqualTo(LocalDateTime.now());

        // then: OrderRepository.save() 호출
        verify(orderRepository).save(pendingOrder);
    }

    @Test
    void cancelOrder_whenNotPending_throwsInvalidOrderStatusChangeException() {
        // given: SHIPPING 상태 주문 반환
        when(orderRepository.findById(2L)).thenReturn(Optional.of(shippingOrder));

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(2L, null))
                .isInstanceOf(InvalidOrderStatusChangeException.class)
                .hasMessage("배송 전 주문만 취소 가능합니다.");

        // then: 취소 기록도, 주문 저장도 호출되지 않아야 함
        verify(canceledOrderRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_whenNotFound_throwsResourceNotFoundException() {
        // given: 주문 없음
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(99L, "테스트 이유"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("주문을 찾을 수 없습니다.");

        // then: 아무 save 호출 없어야 함
        verify(canceledOrderRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }
}