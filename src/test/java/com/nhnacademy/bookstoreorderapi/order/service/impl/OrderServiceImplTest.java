package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.common.service.PointService;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.client.book.service.BookService;
import com.nhnacademy.bookstoreorderapi.order.common.resolver.XUserIdResolver;
import com.nhnacademy.bookstoreorderapi.order.domain.*;
import com.nhnacademy.bookstoreorderapi.order.dto.request.CreateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.ReturnsRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.UpdateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.CreateOrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderDetailResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.exception.badrequest.InvalidOrderStatusChangeException;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.exception.unauthorized.NotMemberException;
import com.nhnacademy.bookstoreorderapi.order.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private WrappingRepository wrappingRepository;
    @Mock
    private OrderStatusLogRepository statusLogRepository;
    @Mock
    private ReturnsRepository returnRepository;
    @Mock
    private BookService bookService;
    @Mock
    private PointService pointService;
    @Mock
    private XUserIdResolver xUserIdResolver;
    @InjectMocks
    private OrderServiceImpl orderService;

    private final BookResponse bookResponse = new BookResponse(1L, "title", "desc", "toc", "author", "publisher",
            LocalDate.now(), "isbn", 100, 100, true, LocalDateTime.now(), LocalDateTime.now(), "status", 10);

    private final ShippingInfo shippingInfo = new ShippingInfo(
            new UpdateOrderRequest(null, "받는 사람", "010-1234-5678", "주소", LocalDate.now().plusDays(1)),
            ShippingInfo.DEFAULT_SHIPPING_FEE);

    @Test
    @DisplayName("회원 주문 생성에 성공한다")
    void createOrder_member_success() {
        // given
        String memberXUserId = "testUser"; // 회원
        CreateOrderRequest orderRequest =
                new CreateOrderRequest(List.of(new CreateOrderRequest.CreateOrderItemRequest(1L, 1)));
        Order memberOrder = new Order(1L);
        List<OrderItem> memberItems = List.of(new OrderItem(1L, "책제목", 100, 1, memberOrder));

        given(xUserIdResolver.resolveUserNo(anyString())).willReturn(1L);
        given(orderRepository.save(any(Order.class))).willReturn(memberOrder);
        given(orderItemRepository.saveAll(anyList())).willReturn(memberItems);
        given(bookService.getBookOrderResponse(anyList())).willReturn(List.of(bookResponse));

        // when
        CreateOrderResponse memberResult = orderService.createOrder(orderRequest, memberXUserId);

        // then
        assertThat(memberResult.getOrderItems().size()).isEqualTo(1);

        verify(xUserIdResolver, times(1)).resolveUserNo(any());
        verify(orderRepository, times(1)).save(any());
        verify(orderItemRepository, times(1)).saveAll(anyList());
        verify(bookService, times(1)).getBookOrderResponse(anyList());
    }

    @Test
    @DisplayName("비회원 주문 생성에 성공한다")
    void createOrder_guest_success() {
        // given
        CreateOrderRequest orderRequest =
                new CreateOrderRequest(List.of(new CreateOrderRequest.CreateOrderItemRequest(1L, 1)));
        Order guestOrder = new Order(null);
        List<OrderItem> guestItems = List.of(new OrderItem(1L, "책제목", 100, 1, guestOrder));

        given(xUserIdResolver.resolveUserNo(any())).willReturn(null);
        given(orderRepository.save(any(Order.class))).willReturn(guestOrder);
        given(orderItemRepository.saveAll(anyList())).willReturn(guestItems);
        given(bookService.getBookOrderResponse(anyList())).willReturn(List.of(bookResponse));

        // when
        CreateOrderResponse guestResult = orderService.createOrder(orderRequest, null);

        // then
        assertThat(guestResult.getOrderItems().size()).isEqualTo(1);

        verify(xUserIdResolver, times(1)).resolveUserNo(any());
        verify(orderRepository, times(1)).save(any());
        verify(orderItemRepository, times(1)).saveAll(anyList());
        verify(bookService, times(1)).getBookOrderResponse(anyList());
    }

    @Test
    @DisplayName("요청데이터들이 bookId가 똑같은 것이 여러 개 있으면 개수가 합해진다")
    void createOrder_sameBookId_sumAmount() {
        // given
        String xUserId = "testUser";
        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(
                new CreateOrderRequest.CreateOrderItemRequest(1L, 1),
                new CreateOrderRequest.CreateOrderItemRequest(1L, 2)
        ));
        Order order = new Order(1L);
        List<OrderItem> orderItems = List.of(new OrderItem(1L, "책제목", 100, 3, order));

        given(xUserIdResolver.resolveUserNo(anyString())).willReturn(1L);
        given(orderRepository.save(any())).willReturn(order);
        given(orderItemRepository.saveAll(anyList())).willReturn(orderItems);
        given(bookService.getBookOrderResponse(anyList())).willReturn(List.of(bookResponse));

        // when
        CreateOrderResponse result = orderService.createOrder(orderRequest, xUserId);

        // then
        assertThat(result.getOrderItems().size()).isEqualTo(1);
        assertThat(result.getOrderItems().getFirst().getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("완료되지 않은 주문 조회에 성공한다")
    void getUnfinishedOrder_success() {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = "testUser";
        Long userNo = 1L;
        Order order = new Order(userNo);
        List<OrderItem> orderItems = List.of(new OrderItem(1L, "책제목", 100, 1, order));

        given(xUserIdResolver.resolveUserNo(anyString())).willReturn(userNo);
        given(orderRepository.findByOrderNumberAndUserNo(anyString(), anyLong())).willReturn(Optional.of(order));
        given(orderItemRepository.findAllByOrder(any())).willReturn(orderItems);
        given(bookService.getBookOrderResponse(anyList())).willReturn(List.of(bookResponse));

        // when
        CreateOrderResponse unfinishedOrder = orderService.getUnfinishedOrder(orderNumber, xUserId);

        // then
        assertThat(unfinishedOrder.getOrderItems().size()).isEqualTo(1);

        verify(xUserIdResolver, times(1)).resolveUserNo(xUserId);
        verify(orderRepository, times(1)).findByOrderNumberAndUserNo(orderNumber, userNo);
        verify(orderItemRepository, times(1)).findAllByOrder(order);
        verify(bookService, times(1)).getBookOrderResponse(List.of(1L));
    }

    @Test
    @DisplayName("주문 업데이트에 성공한다")
    void updateOrder_success() {
        // given
        String orderNumber = "202507-abcdef-123456";
        UpdateOrderRequest request = new UpdateOrderRequest(
                List.of(
                        new UpdateOrderRequest.WrappingRequest(1L, 1L),
                        new UpdateOrderRequest.WrappingRequest(2L, null)
                ),
                "받는 사람",
                "010-1234-5678",
                "우주",
                LocalDate.now().plusDays(3)
        );

        Order order = new Order(null);
        List<OrderItem> orderItems = List.of(
                new OrderItem(1L, "책제목1", 10000, 1, order),
                new OrderItem(2L, "책제목2", 20000, 1, order)
        );
        Wrapping wrapping = new Wrapping("포장지", 50, true);

        given(xUserIdResolver.resolveUserNo(any())).willReturn(null);
        given(orderRepository.findByOrderNumberAndUserNo(anyString(), any())).willReturn(Optional.of(order));
        given(orderItemRepository.findAllByOrder(any(Order.class))).willReturn(orderItems);
        given(wrappingRepository.findById(anyLong())).willReturn(Optional.of(wrapping));

        // when
        OrderResponse result = orderService.updateOrder(orderNumber, request, null);

        // then
        assertThat(result.getReceiverName()).isEqualTo("받는 사람");
        assertThat(result.getReceiverPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(result.getAddress()).isEqualTo("우주");
        assertThat(result.getRequestedDeliveryDate()).isEqualTo(LocalDate.now().plusDays(3));
        assertThat(result.getShippingFee()).isEqualTo(5_000);

        verify(xUserIdResolver, times(1)).resolveUserNo(null);
        verify(orderRepository, times(1)).findByOrderNumberAndUserNo(orderNumber, null);
        verify(orderItemRepository, times(1)).findAllByOrder(order);
        verify(wrappingRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("회원은 30000원 이상 주문 시 배송비 무료")
    void updateOrder_freeShippingFee() {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = "testUser";
        Long userNo = 1L;
        UpdateOrderRequest request = new UpdateOrderRequest(
                List.of(new UpdateOrderRequest.WrappingRequest(1L, 1L)),
                "받는 사람",
                "010-1234-5678",
                "우주",
                LocalDate.now().plusDays(3)
        );

        Order order = new Order(null);
        List<OrderItem> orderItems = List.of(new OrderItem(1L, "책제목1", 30000, 1, order));
        Wrapping wrapping = new Wrapping("포장지", 50, true);

        given(xUserIdResolver.resolveUserNo(any())).willReturn(userNo);
        given(orderRepository.findByOrderNumberAndUserNo(anyString(), any())).willReturn(Optional.of(order));
        given(orderItemRepository.findAllByOrder(any(Order.class))).willReturn(orderItems);
        given(wrappingRepository.findById(anyLong())).willReturn(Optional.of(wrapping));

        // when
        OrderResponse result = orderService.updateOrder(orderNumber, request, xUserId);

        // then
        assertThat(result.getReceiverName()).isEqualTo("받는 사람");
        assertThat(result.getReceiverPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(result.getAddress()).isEqualTo("우주");
        assertThat(result.getRequestedDeliveryDate()).isEqualTo(LocalDate.now().plusDays(3));
        assertThat(result.getShippingFee()).isEqualTo(0);

        verify(xUserIdResolver, times(1)).resolveUserNo(xUserId);
        verify(orderRepository, times(1)).findByOrderNumberAndUserNo(orderNumber, userNo);
        verify(orderItemRepository, times(1)).findAllByOrder(order);
        verify(wrappingRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("주문 상세 조회에 성공한다")
    void findByOrderNumber_success() {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = "testUser";
        Long userNo = 1L;

        Order order = new Order(userNo);
        order.setStatus(OrderStatus.PENDING_PAY);
        order.setShippingInfo(shippingInfo);
        List<OrderItem> orderItems = List.of(new OrderItem(1L, "책제목", 1_000, 1, order));

        given(xUserIdResolver.resolveUserNo(anyString())).willReturn(userNo);
        given(orderRepository.findByOrderNumberAndUserNo(anyString(), anyLong())).willReturn(Optional.of(order));
        given(orderItemRepository.findAllByOrder(any(Order.class))).willReturn(orderItems);
        given(bookService.getBookOrderResponse(anyList())).willReturn(List.of(bookResponse));

        // when
        OrderDetailResponse result = orderService.findByOrderNumber(orderNumber, xUserId);

        // then
        assertThat(result.getReceiverName()).isEqualTo("받는 사람");
        assertThat(result.getReceiverPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(result.getAddress()).isEqualTo("주소");
        assertThat(result.getDeliveryFee()).isEqualTo(5_000);

        verify(xUserIdResolver, times(1)).resolveUserNo(anyString());
        verify(orderRepository, times(1)).findByOrderNumberAndUserNo(anyString(), anyLong());
        verify(orderItemRepository, times(1)).findAllByOrder(any(Order.class));
        verify(bookService, times(1)).getBookOrderResponse(anyList());
    }

    @Test
    @DisplayName("반품 요청에 성공한다")
    void changeStatusToReturned_success() {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = "testUser";
        Long userNo = 1L;
        ReturnsRequest request = new ReturnsRequest("상품 불량", true);

        Order order = new Order(userNo);
        order.setStatus(OrderStatus.COMPLETED);
        order.setShippingInfo(shippingInfo);

        OrderReturn returnedOrder = new OrderReturn(order, "파손됨", true);

        given(xUserIdResolver.resolveUserNo(anyString())).willReturn(userNo);
        given(orderRepository.findByOrderNumber(anyString())).willReturn(Optional.of(order));
        given(statusLogRepository.canReturnOrder(any(Order.class), anyBoolean())).willReturn(true);
        given(statusLogRepository.getCompletedOrderPaymentAmount(any(Order.class), anyBoolean())).willReturn(Optional.of(15_000L));
        doNothing().when(pointService).processPointRefund(any(Order.class), anyLong());
        given(returnRepository.save(any(OrderReturn.class))).willReturn(returnedOrder);

        // when
        OrderResponse result = orderService.changeStatusToReturned(orderNumber, request, xUserId);

        // then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.RETURNED.name());

        verify(xUserIdResolver, times(1)).resolveUserNo(anyString());
        verify(orderRepository, times(1)).findByOrderNumber(anyString());
        verify(statusLogRepository, times(1)).canReturnOrder(any(Order.class), anyBoolean());
        verify(statusLogRepository, times(1)).getCompletedOrderPaymentAmount(any(Order.class), anyBoolean());
        verify(pointService, times(1)).processPointRefund(any(Order.class), anyLong());
        verify(returnRepository, times(1)).save(any(OrderReturn.class));
        verify(statusLogRepository, times(1)).save(any(OrderStatusLog.class));
    }

    @Test
    @DisplayName("회원 주문 전체 조회에 성공한다")
    void findAllByUserId_success() {
        // given
        String xUserId = "testUser";
        Long userNo = 1L;
        Pageable pageable = Pageable.unpaged();

        given(xUserIdResolver.resolveUserNo(anyString())).willReturn(userNo);
        given(orderRepository.findOrderSummary(anyLong(), any(Pageable.class))).willReturn(Page.empty());

        // when
        Page<OrderSummaryResponse> result = orderService.findAllByUserId(xUserId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();

        verify(xUserIdResolver, times(1)).resolveUserNo(anyString());
        verify(orderRepository, times(1)).findOrderSummary(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("비회원이 반품 요청 시 실패한다")
    void changeStatusToReturned_notMember_fail() {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = null;
        ReturnsRequest request = new ReturnsRequest("상품 불량", true);

        given(xUserIdResolver.resolveUserNo(any())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> orderService.changeStatusToReturned(orderNumber, request, xUserId))
                .isInstanceOf(NotMemberException.class);

        verify(xUserIdResolver, times(1)).resolveUserNo(xUserId);
        verify(orderRepository, never()).findByOrderNumber(anyString());
    }

    @Test
    @DisplayName("존재하지 않는 주문번호로 반품 요청 시 실패한다")
    void changeStatusToReturned_orderNotFound_fail() {
        // given
        String orderNumber = "not-found";
        String xUserId = "testUser";
        Long userNo = 1L;
        ReturnsRequest request = new ReturnsRequest("상품 불량", true);

        given(xUserIdResolver.resolveUserNo(anyString())).willReturn(userNo);
        given(orderRepository.findByOrderNumber(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.changeStatusToReturned(orderNumber, request, xUserId))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, times(1)).findByOrderNumber(orderNumber);
    }

    @Test
    @DisplayName("반품 불가능한 주문에 대해 반품 요청 시 실패한다")
    void changeStatusToReturned_cannotReturn_fail() {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = "testUser";
        Long userNo = 1L;
        ReturnsRequest request = new ReturnsRequest("상품 불량", true);

        Order order = new Order(userNo);
        order.setStatus(OrderStatus.PENDING_PAY);

        given(xUserIdResolver.resolveUserNo(anyString())).willReturn(userNo);
        given(orderRepository.findByOrderNumber(anyString())).willReturn(Optional.of(order));
        given(statusLogRepository.canReturnOrder(any(Order.class), anyBoolean())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> orderService.changeStatusToReturned(orderNumber, request, xUserId))
                .isInstanceOf(InvalidOrderStatusChangeException.class);

        verify(statusLogRepository, times(1)).canReturnOrder(order, request.damaged());
    }
}
