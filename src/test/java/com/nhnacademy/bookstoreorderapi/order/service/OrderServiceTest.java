package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.InvalidOrderStatusChangeException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.ResourceNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private WrappingRepository wrappingRepository;
    @Mock private CanceledOrderRepository canceledOrderRepository;
    @Mock private OrderStatusLogRepository statusLogRepository;

    @Mock
    private WrappingRepository wrappingRepository;

    @InjectMocks
    private OrderService orderService;


    private OrderRequestDto baseGuestRequest;
    private OrderRequestDto baseMemberRequest;

    @BeforeEach
    void setUp() {
        baseGuestRequest = OrderRequestDto.builder()
                .orderType("guest")
                .guestName("테스트")
                .guestPhone("010-0000-0000")
                .deliveryDate(null)
                .items(Collections.singletonList(
                        OrderItemDto.builder()
                                .bookId(1L)
                                .quantity(2)
                                .giftWrapped(false)
                                .build()
                ))
                .build();

        baseMemberRequest = OrderRequestDto.builder()
                .orderType("member")
                .userId("100L")
                .deliveryDate(null)
                .items(Collections.singletonList(
                        OrderItemDto.builder()
                                .bookId(2L)
                                .quantity(1)
                                .giftWrapped(false)
                                .build()
                ))
                .build();
    }

    @ParameterizedTest(name = "주문#{0} 상태를 {1}->{2}로 변경하면 성공")
    @CsvSource({
            "1, PENDING, SHIPPING",
            "2, PENDING, CANCELED",
            "3, SHIPPING, COMPLETED"
    })
    void changeStatus_success(long orderId, OrderStatus oldStatus, OrderStatus newStatus) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(oldStatus);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        StatusChangeResponseDto resp = orderService.changeStatus(orderId, newStatus, 42L, "메모");

        assertThat(resp.getOrderId()).isEqualTo(orderId);
        assertThat(resp.getOldStatus()).isEqualTo(oldStatus);
        assertThat(resp.getNewStatus()).isEqualTo(newStatus);
        assertThat(resp.getChangedBy()).isEqualTo(42L);
        assertThat(resp.getMemo()).isEqualTo("메모");
        assertThat(resp.getChangedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        verify(statusLogRepository).save(any(OrderStatusLog.class));
        verify(orderRepository).save(order);
    }

    @ParameterizedTest(name = "주문#{0} 상태를 {1}->{2}로 변경 시 예외")
    @CsvSource({
            "1, PENDING, COMPLETED",
            "2, SHIPPING, PENDING"
    })
    void changeStatus_fail(long orderId, OrderStatus oldStatus, OrderStatus newStatus) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(oldStatus);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
                orderService.changeStatus(orderId, newStatus, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("상태 전이 불가");

        verify(statusLogRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_whenDeliveryDateNull_setsToday() {
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order o = invocation.getArgument(0);
                    o.setId(100L);
                    return o;
                });

        OrderResponseDto resp = orderService.createOrder(baseGuestRequest);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getDeliveryAt())
                .isEqualTo(LocalDate.now().atStartOfDay());
        assertThat(resp.getMessage()).contains("총액").contains("배송비");
    }

    @Test
    void createOrder_whenDeliveryDateSpecified_setsCustom() {
        LocalDate custom = LocalDate.of(2025, 6, 15);
        baseMemberRequest.setDeliveryDate(custom);
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order o = invocation.getArgument(0);
                    o.setId(200L);
                    return o;
                });

        OrderResponseDto resp = orderService.createOrder(baseMemberRequest);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getDeliveryAt())
                .isEqualTo(custom.atStartOfDay());
    }

    @Test
    void cancelOrder_whenPending_savesRecordAndUpdatesStatus() {
        Order order = new Order(); order.setId(1L); order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1L, "사유");

        verify(canceledOrderRepository).save(any(CanceledOrder.class));
        verify(orderRepository).save(order);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("존재하지 않는 주문 ID로 반품 요청 시 예외 발생")
    void returnRequest_OrderNotFound() {

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.requestReturn(1L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("주문을 찾을 수 없습니다");
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("이미 반품 처리된 주문에 또다시 반품 요청 시 예외 발생")
    void returnRequest_AlreadyReturned() {

        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.RETURNED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.requestReturn(1L))
                .isInstanceOf(InvalidOrderStatusChangeException.class)
                .hasMessageContaining("이미 반품된 상품입니다.");
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("POST /orders/{orderId}/returns - 200 OK")
    void returnRequest_Success() {

        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.COMPLETED);
        order.setTotalPrice(10000);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        int returnPrice = orderService.requestReturn(1L);

        assertThat(returnPrice).isEqualTo(7500);
    }
}

    @Test
    void cancelOrder_whenNotPending_throws() {
        Order order = new Order(); order.setId(2L); order.setStatus(OrderStatus.SHIPPING);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(2L, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("배송 전 주문만 취소 가능합니다.");

        verify(canceledOrderRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getStatusLog_success_returnsDtoList() {
        OrderStatusLog log = OrderStatusLog.builder()
                .orderStateId(1L).orderId(1L)
                .oldStatus(OrderStatus.PENDING).newStatus(OrderStatus.SHIPPING)
                .changedAt(LocalDateTime.now()).changedBy(42L).memo("메모").build();
        when(orderRepository.existsById(1L)).thenReturn(true);
        when(statusLogRepository.findByOrderId(1L)).thenReturn(List.of(log));

        List<OrderStatusLogDto> logs = orderService.getStatusLog(1L);

        assertThat(logs).hasSize(1)
                .first()
                .extracting(OrderStatusLogDto::getOrderStateId, OrderStatusLogDto::getNewStatus)
                .containsExactly(1L, OrderStatus.SHIPPING);
    }

    @Test
    void getStatusLog_whenNotFound_throws() {
        when(orderRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.getStatusLog(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("주문을 찾을 수 없습니다.");
    }
}