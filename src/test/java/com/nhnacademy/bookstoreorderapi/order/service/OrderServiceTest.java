// src/test/java/com/nhnacademy/bookstoreorderapi/order/service/OrderServiceTest.java
package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatusLog;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Wrapping;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.BadRequestException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.InvalidOrderStatusChangeException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.ResourceNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.repository.CanceledOrderRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderStatusLogRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.WrappingRepository;
import com.nhnacademy.bookstoreorderapi.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private WrappingRepository wrappingRepository;
    @Mock
    private CanceledOrderRepository canceledOrderRepository;
    @Mock
    private OrderStatusLogRepository statusLogRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderRequestDto guestReq;
    private OrderRequestDto memberReq;
    private Wrapping wrap;

    @BeforeEach
    void setUp() {
        guestReq = OrderRequestDto.builder()
                .orderType("guest")
                .guestName("홍길동")
                .guestPhone("010-1111-2222")
                .items(List.of(
                        OrderItemDto.builder()
                                .bookId(100L)
                                .quantity(2)
                                .giftWrapped(false)
                                .build()
                ))
                .build();

        memberReq = OrderRequestDto.builder()
                .orderType("member")
                .userId("member42")
                .deliveryDate(LocalDate.of(2025, 6, 20))
                .items(List.of(
                        OrderItemDto.builder()
                                .bookId(200L)
                                .quantity(3)
                                .giftWrapped(true)
                                .wrappingId(1L)
                                .build()
                ))
                .build();

        wrap = Wrapping.builder()
                .wrappingId(1L)
                .name("프리미엄")
                .price(3000)
                .build();
    }

    @Test
    void createOrder_guest_succeeds() {
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setOrderId(5L);
            return o;
        });

        OrderResponseDto resp = orderService.createOrder(guestReq);

        assertThat(resp.getOrderId()).isEqualTo(5L);
        assertThat(resp.getTotalPrice()).isEqualTo(2 * 10_000);
        assertThat(resp.getDeliveryFee()).isEqualTo(5_000);
        assertThat(resp.getFinalPrice()).isEqualTo(25_000);
    }

    @Test
    void createOrder_member_withWrapping_succeeds() {
        when(wrappingRepository.findById(1L)).thenReturn(Optional.of(wrap));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setOrderId(6L);
            return o;
        });

        OrderResponseDto resp = orderService.createOrder(memberReq);

        // 3 * 10000 + 3*3000 = 39000, 회원 무료 배송
        assertThat(resp.getTotalPrice()).isEqualTo(39_000);
        assertThat(resp.getDeliveryFee()).isZero();
        assertThat(resp.getFinalPrice()).isEqualTo(39_000);
        assertThat(resp.getOrderId()).isEqualTo(6L);
    }

    @Test
    void createOrder_invalidWrapping_throws() {
        when(wrappingRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.createOrder(memberReq))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("유효하지 않은 포장 ID: 1");
    }

    @Test
    void listAll_returnsMappedDtos() {
        Order o = Order.builder()
                .orderId(7L)
                .userId("u1")
                .status(OrderStatus.PENDING)
                .totalPrice(100)
                .deliveryFee(10)
                .finalPrice(110)
                .build();
        when(orderRepository.findAll()).thenReturn(List.of(o));

        List<OrderResponseDto> list = orderService.listAll();

        assertThat(list).hasSize(1)
                .first()
                .extracting(OrderResponseDto::getOrderId, OrderResponseDto::getFinalPrice)
                .containsExactly(7L, 110);
    }

    @Test
    void changeStatus_validTransition_succeedsAndLogs() {
        Order o = Order.builder().orderId(8L).status(OrderStatus.PENDING).build();
        when(orderRepository.findById(8L)).thenReturn(Optional.of(o));

        StatusChangeResponseDto dto = orderService.changeStatus(8L, OrderStatus.SHIPPING, 123L, "ok");

        verify(statusLogRepository).save(any(OrderStatusLog.class));
        assertThat(dto.getOldStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(dto.getNewStatus()).isEqualTo(OrderStatus.SHIPPING);
    }

    @Test
    void changeStatus_invalidTransition_throws() {
        Order o = Order.builder().orderId(9L).status(OrderStatus.PENDING).build();
        when(orderRepository.findById(9L)).thenReturn(Optional.of(o));

        assertThatThrownBy(() -> orderService.changeStatus(9L, OrderStatus.COMPLETED, 1L, "no"))
                .isInstanceOf(InvalidOrderStatusChangeException.class)
                .hasMessageContaining("상태 전이 불가: PENDING → COMPLETED");
    }

    @Test
    void requestReturn_fromCompleted_returnsRefund() {
        Order o = Order.builder().orderId(10L).status(OrderStatus.COMPLETED).totalPrice(50_000).build();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(o));

        int refund = orderService.requestReturn(10L);

        assertThat(refund).isEqualTo(50_000 - 2_500);
        assertThat(o.getStatus()).isEqualTo(OrderStatus.RETURNED);
    }

    @Test
    void requestReturn_notFound_throws() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.requestReturn(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("주문을 찾을 수 없습니다.");
    }

    @Test
    void getStatusLog_notFound_throws() {
        when(orderRepository.existsById(20L)).thenReturn(false);
        assertThatThrownBy(() -> orderService.getStatusLog(20L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("주문을 찾을 수 없습니다.");
    }

    @Test
    void getStatusLog_returnsDtos() {
        when(orderRepository.existsById(30L)).thenReturn(true);
        OrderStatusLog log = OrderStatusLog.builder()
                .orderStateId(100L)
                .orderId(30L)
                .oldStatus(OrderStatus.PENDING)
                .newStatus(OrderStatus.SHIPPING)
                .changedAt(LocalDateTime.now())
                .changedBy(55L)
                .memo("go")
                .build();
        when(statusLogRepository.findByOrderId(30L)).thenReturn(List.of(log));

        List<OrderStatusLogDto> list = orderService.getStatusLog(30L);
        assertThat(list).singleElement()
                .extracting(OrderStatusLogDto::getOrderStateId, OrderStatusLogDto::getNewStatus)
                .containsExactly(100L, OrderStatus.SHIPPING);
    }
}