package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.*;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.*;
import com.nhnacademy.bookstoreorderapi.order.repository.*;
import com.nhnacademy.bookstoreorderapi.order.service.impl.OrderServiceImpl;
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

    @Mock private OrderRepository         orderRepository;
    @Mock private CanceledOrderRepository canceledOrderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

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

    // PENDING → 취소 정상 흐름
    @Test
    void cancelOrder_whenPending_savesCanceledRecordAndUpdatesStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        ArgumentCaptor<CanceledOrder> captor = ArgumentCaptor.forClass(CanceledOrder.class);

        orderService.cancelOrder(1L, "고객 요청");

        assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);

        verify(canceledOrderRepository).save(captor.capture());
        CanceledOrder record = captor.getValue();
        assertThat(record.getOrderId()).isEqualTo(1L);
        assertThat(record.getReason()).isEqualTo("고객 요청");
        assertThat(record.getCanceledAt()).isBeforeOrEqualTo(LocalDateTime.now());

        verify(orderRepository).save(pendingOrder);
    }

    // PENDING 이 아닐 때 예외
    @Test
    void cancelOrder_whenNotPending_throwsInvalidOrderStatusChangeException() {
        when(orderRepository.findById(2L)).thenReturn(Optional.of(shippingOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(2L, null))
                .isInstanceOf(InvalidOrderStatusChangeException.class)
                .hasMessage("배송 전(PENDING) 상태만 취소 가능합니다.");

        verify(canceledOrderRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    // 3. 주문이 존재하지 않을 때
    @Test
    void cancelOrder_whenNotFound_throwsResourceNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(99L, "테스트 이유"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("주문을 찾을 수 없습니다.");

        verify(canceledOrderRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }
}