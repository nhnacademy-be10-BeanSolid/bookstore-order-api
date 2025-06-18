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
import org.mockito.*;
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
    private OrderServiceImpl orderService;

    private OrderRequestDto guestReq;
    private OrderRequestDto memberReq;
    private Wrapping        wrap;

    @BeforeEach
    void setUp() {
        guestReq = OrderRequestDto.builder()
                .orderType("guest")
                .guestId(1L)
                .items(List.of(
                        OrderItemDto.builder()
                                .bookId(100L)
                                .quantity(2)
                                .giftWrapped(false)
                                .build()))
                .build();

        memberReq = OrderRequestDto.builder()
                .orderType("member")
                .userId("member42")
                .requestedDeliveryDate(LocalDate.of(2025, 6, 20))
                .items(List.of(
                        OrderItemDto.builder()
                                .bookId(200L)
                                .quantity(3)
                                .giftWrapped(true)
                                .wrappingId(1L)
                                .build()))
                .build();

        wrap = Wrapping.builder()
                .wrappingId(1L)
                .name("프리미엄")
                .price(3_000)
                .build();
    }

    // 주문 생성

    @Test
    void createOrder_guest_succeeds() {
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);

        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(5L);
            return o;
        });

        OrderResponseDto resp = orderService.createOrder(guestReq);

        assertThat(resp.getOrderId()).isEqualTo(5L);
        assertThat(resp.getTotalPrice()).isEqualTo(2 * 10_000);
        assertThat(resp.getDeliveryFee()).isEqualTo(Order.DEFAULT_DELIVERY_FEE); // 5 000
    }

    @Test
    void createOrder_memberWithWrapping_succeeds() {
        when(wrappingRepository.findById(1L)).thenReturn(Optional.of(wrap));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(6L);
            return o;
        });

        OrderResponseDto resp = orderService.createOrder(memberReq);

        // 3 × 10 000 + 3 × 3 000 = 39 000  (회원 무료 배송)
        assertThat(resp.getTotalPrice()).isEqualTo(39_000);
        assertThat(resp.getDeliveryFee()).isZero();
        assertThat(resp.getOrderId()).isEqualTo(6L);
    }

    @Test
    void createOrder_invalidWrapping_throwsBadRequest() {
        when(wrappingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(memberReq))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("잘못된 wrappingId : 1");
    }

    // 상태 변경

    @Test
    void changeStatus_validTransition_logsAndUpdates() {
        Order o = Order.builder().id(8L).status(OrderStatus.PENDING).build();
        when(orderRepository.findById(8L)).thenReturn(Optional.of(o));
        when(statusLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StatusChangeResponseDto dto =
                orderService.changeStatus(8L, OrderStatus.SHIPPING, 123L, "ok");

        verify(statusLogRepository).save(any(OrderStatusLog.class));
        assertThat(dto.getOldStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(dto.getNewStatus()).isEqualTo(OrderStatus.SHIPPING);
        assertThat(o.getStatus()).isEqualTo(OrderStatus.SHIPPING);
    }

    @Test
    void changeStatus_invalidTransition_throws() {
        Order o = Order.builder().id(9L).status(OrderStatus.PENDING).build();
        when(orderRepository.findById(9L)).thenReturn(Optional.of(o));

        assertThatThrownBy(() ->
                orderService.changeStatus(9L, OrderStatus.COMPLETED, 1L, "no"))
                .isInstanceOf(InvalidOrderStatusChangeException.class)
                .hasMessageContaining("상태 전이 불가 : PENDING → COMPLETED");
    }

    // 반품 요청

    @Test
    void requestReturn_fromCompleted_returnsRefund() {
        Order o = Order.builder().id(10L).status(OrderStatus.COMPLETED).totalPrice(50000).build();
        ReturnRequestDto dto = ReturnRequestDto.builder()
                .reason("테스트 이유")
                .requestedAt(LocalDateTime.now())
                .damaged(false)
                .build();
        OrderReturn orderReturn = OrderReturn.createFrom(o, dto);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(o));
        when(orderRepository.save(any(Order.class))).thenReturn(o);
        when(returnRepository.save(any(OrderReturn.class))).thenReturn(orderReturn);

        int refund = orderService.requestReturn(10L, dto);

        assertThat(refund).isEqualTo(50000 - OrderReturn.RETURNS_FEE);
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

    // 상태 이력 조회

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
                .extracting(OrderStatusLogDto::getOrderStateId,
                        OrderStatusLogDto::getNewStatus)
                .containsExactly(100L, OrderStatus.SHIPPING);
    }

    @Test
    @DisplayName("상태를 '대기->배송중'으로 변경 후 5초 뒤 '배송완료'로 자동 변경에 성공")
    void autoDeliveryComplete_Success() {

        TaskScheduler scheduler = new ConcurrentTaskScheduler();

        Order testOrder = new Order();
        testOrder.setId(1L);
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

        orderService.changeStatus(testOrder.getId(), OrderStatus.SHIPPING, changedBy, memo);

        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }
}