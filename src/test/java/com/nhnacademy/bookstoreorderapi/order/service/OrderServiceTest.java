// src/test/java/com/nhnacademy/bookstoreorderapi/order/service/OrderServiceTest.java
package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.*;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.BadRequestException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.InvalidOrderStatusChangeException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.ResourceNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.repository.*;
import com.nhnacademy.bookstoreorderapi.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    WrappingRepository wrappingRepository;
    @Mock
    CanceledOrderRepository canceledOrderRepository;
    @Mock
    OrderStatusLogRepository statusLogRepository;
    @Mock
    TaskScheduler taskScheduler;
    @Mock
    ReturnsRepository returnRepository;

    @InjectMocks
    OrderServiceImpl orderService;

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
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);

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
        Order o = Order.builder().orderId(10L).status(OrderStatus.COMPLETED).totalPrice(50000).build();
        ReturnRequestDto dto = ReturnRequestDto.builder()
                .reason("테스트 이유")
                .requestedAt(LocalDateTime.now())
                .damaged(false)
                .build();
        Returns returns = Returns.createFrom(o, dto);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(o));
        when(orderRepository.save(any(Order.class))).thenReturn(o);
        when(returnRepository.save(any(Returns.class))).thenReturn(returns);

        int refund = orderService.requestReturn(10L, dto);

        assertThat(refund).isEqualTo(50000 - Returns.RETURNS_FEE);
        assertThat(o.getStatus()).isEqualTo(OrderStatus.RETURNED);
    }

    @Test
    void requestReturn_notFound_throws() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        ReturnRequestDto dto = ReturnRequestDto.builder()
                .reason("테스트 이유")
                .requestedAt(LocalDateTime.now())
                .damaged(false)
                .build();

        assertThatThrownBy(() -> orderService.requestReturn(99L, dto))
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

    @Test
    @DisplayName("상태를 '대기->배송중'으로 변경 후 5초 뒤 '배송완료'로 자동 변경에 성공")
    void autoDeliveryComplete_Success() {

        TaskScheduler scheduler = new ConcurrentTaskScheduler();

        Order testOrder = new Order();
        testOrder.setOrderId(1L);
        testOrder.setStatus(OrderStatus.PENDING);

        Long changedBy = 99L;
        String memo = "배송 자동 완료 테스트";

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        doAnswer(invocationOnMock -> {
            Runnable task = invocationOnMock.getArgument(0);
            task.run();
            return null;
        }).when(taskScheduler).schedule(any(Runnable.class), any(Date.class));

        doAnswer(invocationOnMock -> {
            return null;
        }).when(statusLogRepository).save(any(OrderStatusLog.class));

        orderService.changeStatus(testOrder.getOrderId(), OrderStatus.SHIPPING, changedBy, memo);

        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }
}

