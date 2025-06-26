package com.nhnacademy.bookstoreorderapi.order.service.impl;

//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
import com.nhnacademy.bookstoreorderapi.order.client.book.BookServiceClient;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookOrderResponse;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderItem;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Wrapping;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderItemRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderItemRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.WrappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

//import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

//
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    BookServiceClient bookServiceClient;
    @Mock
    WrappingRepository wrappingRepository;
    @Mock
    OrderItemRepository orderItemRepository;
//    @Mock
//    CanceledOrderRepository canceledOrderRepository;
//    @Mock
//    OrderStatusLogRepository statusLogRepository;
//    @Mock
//    TaskScheduler taskScheduler;
//    @Mock
//    ReturnsRepository returnRepository;
//
    @InjectMocks
    private OrderServiceImpl orderService;

    //
//    private OrderRequestDto guestReq;
//    private OrderRequestDto memberReq;
//    private Wrapping        wrap;
//
//    @BeforeEach
//    void setUp() {
//
//        List<OrderItemRequest> items = List.of(new OrderItemRequest(1L, 1, 1L),
//                new OrderItemRequest(2L, 1, 1L));
//
//        OrderRequest orderRequest = OrderRequest.builder()
//                .address("광주광역시")
//                .requestedDeliveryDate(LocalDate.now().plusDays(1))
//                .items(items)
//                .build();
//
//    }

    //        guestReq = OrderRequestDto.builder()
//                .orderType("guest")
//                .guestId(1L)
//                .items(List.of(
//                        OrderItemRequestDto.builder()
//                                .bookId(100L)
//                                .quantity(2)
//                                .giftWrapped(false)
//                                .build()))
//                .build();
//
//        memberReq = OrderRequestDto.builder()
//                .orderType("member")
//                .userId("member42")
//                .requestedDeliveryDate(LocalDate.of(2025, 6, 20))
//                .items(List.of(
//                        OrderItemRequestDto.builder()
//                                .bookId(200L)
//                                .quantity(3)
//                                .giftWrapped(true)
//                                .wrappingId(1L)
//                                .build()))
//                .build();
//
//        wrap = Wrapping.builder()
//                .wrappingId(1L)
//                .name("프리미엄")
//                .price(3_000)
//                .build();
//
//
    @Test
    @DisplayName("회원/비회원 주문 생성에 성공한다")
    void createOrder_guest_succeeds() {

        // 포장지 생성
        Wrapping wrap1 = new Wrapping();
        wrap1.setId(1L);

        // 주문 요청 생성
        List<OrderItemRequest> items = List.of(new OrderItemRequest(1L, 1, 1L));
        OrderRequest orderRequest = new OrderRequest("받는사람", "광주광역시", "010-1111-2222", LocalDate.now().plusDays(1), items);

        // 회원/비회원 주문 생성
        Long memberId = 1L;
        Long guestId = null;

        // 도서 도메인으로부터의 응답
        ResponseEntity<List<BookOrderResponse>> bookOrderResponses = ResponseEntity.ok(List.of(new BookOrderResponse(1L, 10_000, 1)));

        given(orderRepository.save(any(Order.class))).willReturn(null);
        given(bookServiceClient.getBookOrderResponse(anyList())).willReturn(bookOrderResponses);
        given(wrappingRepository.findAllById(anyList())).willReturn(List.of(wrap1));
        given(wrappingRepository.saveAll(any())).willReturn(List.of(wrap1));
        given(orderItemRepository.saveAll(any())).willReturn(List.of(OrderItem.of(bookOrderResponses.getBody().getFirst(), 1)));

        // when
        orderService.createOrder(orderRequest, memberId);
        orderService.createOrder(orderRequest, guestId);

        then(orderRepository).should(times(2)).save(any(Order.class));
        then(bookServiceClient).should(times(2)).getBookOrderResponse(anyList());
        then(wrappingRepository).should(times(2)).findAllById(anyList());
        then(wrappingRepository).should(times(2)).saveAll(any());
        then(orderItemRepository).should(times(2)).saveAll(anyList());
    }
//
//    @Test
//    void createOrder_invalidWrapping_throwsBadRequest() {
//        when(wrappingRepository.findById(1L)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> orderService.createOrder(memberReq))
//                .isInstanceOf(BadRequestException.class)
//                .hasMessageContaining("잘못된 wrappingId : 1");
//    }
//
//    // 상태 변경
//
//    @Test
//    void changeStatus_validTransition_logsAndUpdates() {
//        Order o = Order.builder().id(8L).status(OrderStatus.PENDING).build();
//        when(orderRepository.findById(8L)).thenReturn(Optional.of(o));
//        when(statusLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
//
//        StatusChangeResponseDto dto =
//                orderService.changeStatus(8L, OrderStatus.SHIPPING, 123L, "ok");
//
//        verify(statusLogRepository).save(any(OrderStatusLog.class));
//        assertThat(dto.getOldStatus()).isEqualTo(OrderStatus.PENDING);
//        assertThat(dto.getNewStatus()).isEqualTo(OrderStatus.SHIPPING);
//        assertThat(o.getStatus()).isEqualTo(OrderStatus.SHIPPING);
//    }
//
//    @Test
//    void changeStatus_invalidTransition_throws() {
//        Order o = Order.builder().id(9L).status(OrderStatus.PENDING).build();
//        when(orderRepository.findById(9L)).thenReturn(Optional.of(o));
//
//        assertThatThrownBy(() ->
//                orderService.changeStatus(9L, OrderStatus.COMPLETED, 1L, "no"))
//                .isInstanceOf(InvalidOrderStatusChangeException.class)
//                .hasMessageContaining("상태 전이 불가 : PENDING → COMPLETED");
//    }
//
//    // 반품 요청
//
//    @Test
//    void requestReturn_fromCompleted_returnsRefund() {
//        Order o = Order.builder().id(10L).status(OrderStatus.COMPLETED).totalPrice(50000).build();
//        ReturnRequestDto dto = ReturnRequestDto.builder()
//                .reason("테스트 이유")
//                .requestedAt(LocalDateTime.now())
//                .damaged(false)
//                .build();
//        OrderReturn orderReturn = OrderReturn.createFrom(o, dto);
//
//        when(orderRepository.findById(10L)).thenReturn(Optional.of(o));
//        when(orderRepository.save(any(Order.class))).thenReturn(o);
//        when(returnRepository.save(any(OrderReturn.class))).thenReturn(orderReturn);
//
//        int refund = orderService.requestReturn(10L, dto);
//
//        assertThat(refund).isEqualTo(50000 - OrderReturn.RETURNS_FEE);
//        assertThat(o.getStatus()).isEqualTo(OrderStatus.RETURNED);
//    }
//
//    @Test
//    void requestReturn_notFound_throws() {
//        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
//        ReturnRequestDto dto = ReturnRequestDto.builder()
//                .reason("테스트 이유")
//                .requestedAt(LocalDateTime.now())
//                .damaged(false)
//                .build();
//
//        assertThatThrownBy(() -> orderService.requestReturn(99L, dto))
//                .isInstanceOf(ResourceNotFoundException.class)
//                .hasMessage("주문을 찾을 수 없습니다.");
//    }
//
//    // 상태 이력 조회
//
//    @Test
//    void getStatusLog_notFound_throws() {
//        when(orderRepository.existsById(20L)).thenReturn(false);
//
//        assertThatThrownBy(() -> orderService.getStatusLog(20L))
//                .isInstanceOf(ResourceNotFoundException.class)
//                .hasMessage("주문을 찾을 수 없습니다.");
//    }
//
//    @Test
//    void getStatusLog_returnsDtos() {
//        when(orderRepository.existsById(30L)).thenReturn(true);
//        OrderStatusLog log = OrderStatusLog.builder()
//                .orderStateId(100L)
//                .orderId(30L)
//                .oldStatus(OrderStatus.PENDING)
//                .newStatus(OrderStatus.SHIPPING)
//                .changedAt(LocalDateTime.now())
//                .changedBy(55L)
//                .memo("go")
//                .build();
//        when(statusLogRepository.findByOrderId(30L)).thenReturn(List.of(log));
//
//        List<OrderStatusLogDto> list = orderService.getStatusLog(30L);
//
//        assertThat(list).singleElement()
//                .extracting(OrderStatusLogDto::getOrderStateId,
//                        OrderStatusLogDto::getNewStatus)
//                .containsExactly(100L, OrderStatus.SHIPPING);
//    }
//
//    @Test
//    @DisplayName("상태를 '대기->배송중'으로 변경 후 5초 뒤 '배송완료'로 자동 변경에 성공")
//    void autoDeliveryComplete_Success() {
//
//        TaskScheduler scheduler = new ConcurrentTaskScheduler();
//
//        Order testOrder = new Order();
//        testOrder.setId(1L);
//        testOrder.setStatus(OrderStatus.PENDING);
//
//        Long changedBy = 99L;
//        String memo = "배송 자동 완료 테스트";
//
//        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
//
//        doAnswer(invocationOnMock -> {
//            Runnable task = invocationOnMock.getArgument(0);
//            task.run();
//            return null;
//        }).when(taskScheduler).schedule(any(Runnable.class), any(Date.class));
//
//        doAnswer(invocationOnMock -> {
//            return null;
//        }).when(statusLogRepository).save(any(OrderStatusLog.class));
//
//        orderService.changeStatus(testOrder.getId(), OrderStatus.SHIPPING, changedBy, memo);
//
//        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
//    }
//    }
}