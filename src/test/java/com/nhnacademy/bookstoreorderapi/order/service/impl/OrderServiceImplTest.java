package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.common.service.PointService;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.client.book.service.BookService;
import com.nhnacademy.bookstoreorderapi.order.common.resolver.XUserIdResolver;
import com.nhnacademy.bookstoreorderapi.order.domain.*;
import com.nhnacademy.bookstoreorderapi.order.dto.request.CreateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderStatusRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.UpdateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.*;
import com.nhnacademy.bookstoreorderapi.order.exception.badrequest.InvalidOrderStatusChangeException;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.exception.unauthorized.NotMemberException;
import com.nhnacademy.bookstoreorderapi.order.repository.*;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
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
    private PaymentService paymentService;
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
        Wrapping wrapping = new Wrapping(99L, "포장지", 50, true);

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
        Wrapping wrapping = new Wrapping(99L,"포장지", 50, true);

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
    @DisplayName("반품 요청에 성공한다")
    void handleReturnOrder_success() {
        // given
        String xUserId = "testUser";
        String orderNumber = "202507-abcdef-123456";
        OrderStatusRequest request = new OrderStatusRequest(OrderStatusRequest.OrderAction.RETURN, "파손", true);

        Long userNo = 1L;
        Order order = new Order(userNo);
        order.setShippingInfo(shippingInfo);
        Long refundAmount = 10_000L;
        OrderReturn orderReturn = new OrderReturn(order, request.reason(), request.damaged());
        OrderStatusLog statusLog = new OrderStatusLog(OrderStatus.COMPLETED, OrderStatus.RETURNED, userNo, orderReturn.getReason(), order);

        given(xUserIdResolver.resolveUserNo(anyString())).willReturn(userNo);
        given(orderRepository.findByOrderNumber(anyString())).willReturn(Optional.of(order));
        given(statusLogRepository.canReturnOrder(any(Order.class), anyBoolean())).willReturn(true);
        given(statusLogRepository.getCompletedOrderPaymentAmount(any(Order.class), anyBoolean())).willReturn(Optional.of(refundAmount));
        willDoNothing().given(pointService).processPointRefund(any(Order.class), anyLong());
        given(returnRepository.save(any(OrderReturn.class))).willReturn(orderReturn);
        given(statusLogRepository.save(any(OrderStatusLog.class))).willReturn(statusLog);

        // when
        OrderStatusResult result = orderService.changeOrderStatus(orderNumber, request, xUserId);

        // then
        assertThat(result).isInstanceOf(OrderStatusResult.ReturnResult.class);
        OrderStatusResult.ReturnResult returnResult = (OrderStatusResult.ReturnResult) result;
        assertThat(returnResult.order().getStatus()).isEqualTo(OrderStatus.RETURNED.name());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.RETURNED);
        
        verify(xUserIdResolver).resolveUserNo(xUserId);
        verify(orderRepository).findByOrderNumber(orderNumber);
        verify(statusLogRepository).canReturnOrder(order, true);
        verify(statusLogRepository).getCompletedOrderPaymentAmount(order, true);
        verify(pointService).processPointRefund(order, refundAmount);
        verify(returnRepository).save(any(OrderReturn.class));
        verify(statusLogRepository).save(any(OrderStatusLog.class));
    }

    @Test
    @DisplayName("반품 시 존재하지 않는 주문인 경우 OrderNotFoundException 발생")
    void handleReturnOrder_orderNotFound_throwsOrderNotFoundException() {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = "testUser";
        Long userNo = 1L;
        OrderStatusRequest request = new OrderStatusRequest(OrderStatusRequest.OrderAction.RETURN, "반품 사유", false);

        given(xUserIdResolver.resolveUserNo(xUserId)).willReturn(userNo);
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.changeOrderStatus(orderNumber, request, xUserId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("주문을 찾을 수 없습니다: orderNumber=" + orderNumber);

        verify(xUserIdResolver).resolveUserNo(xUserId);
        verify(orderRepository).findByOrderNumber(orderNumber);
        verify(statusLogRepository, never()).canReturnOrder(any(), anyBoolean());
    }

    @Test
    @DisplayName("반품 시 본인의 주문이 아닌 경우 NotMemberException 발생")
    void handleReturnOrder_notOwner_throwsNotMemberException() {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = "testUser";
        Long userNo = 1L;
        Long otherUserNo = 2L;
        OrderStatusRequest request = new OrderStatusRequest(OrderStatusRequest.OrderAction.RETURN, "반품 사유", false);
        Order otherUserOrder = new Order(otherUserNo);

        given(xUserIdResolver.resolveUserNo(xUserId)).willReturn(userNo);
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(otherUserOrder));

        // when & then
        assertThatThrownBy(() -> orderService.changeOrderStatus(orderNumber, request, xUserId))
                .isInstanceOf(NotMemberException.class)
                .hasMessageContaining("본인의 주문만 반품할 수 있습니다.");

        verify(xUserIdResolver).resolveUserNo(xUserId);
        verify(orderRepository).findByOrderNumber(orderNumber);
        verify(statusLogRepository, never()).canReturnOrder(any(), anyBoolean());
    }

    @Test
    @DisplayName("반품 가능한 기간이 지난 경우 InvalidOrderStatusChangeException 발생")
    void handleReturnOrder_cannotReturn_throwsInvalidOrderStatusChangeException() {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = "testUser";
        Long userNo = 1L;
        OrderStatusRequest request = new OrderStatusRequest(OrderStatusRequest.OrderAction.RETURN, "반품 사유", false);
        Order order = new Order(userNo);

        given(xUserIdResolver.resolveUserNo(xUserId)).willReturn(userNo);
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(order));
        given(statusLogRepository.canReturnOrder(order, false)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> orderService.changeOrderStatus(orderNumber, request, xUserId))
                .isInstanceOf(InvalidOrderStatusChangeException.class)
                .hasMessageContaining("반품 가능한 기간이 지났습니다.");

        verify(xUserIdResolver).resolveUserNo(xUserId);
        verify(orderRepository).findByOrderNumber(orderNumber);
        verify(statusLogRepository).canReturnOrder(order, false);
        verify(statusLogRepository, never()).getCompletedOrderPaymentAmount(any(), anyBoolean());
    }

    @Test
    @DisplayName("반품 가능한 주문이 아닌 경우 InvalidOrderStatusChangeException 발생")
    void handleReturnOrder_invalidOrder_throwsInvalidOrderStatusChangeException() {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = "testUser";
        Long userNo = 1L;
        OrderStatusRequest request = new OrderStatusRequest(OrderStatusRequest.OrderAction.RETURN, "반품 사유", true);
        Order order = new Order(userNo);

        given(xUserIdResolver.resolveUserNo(xUserId)).willReturn(userNo);
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(order));
        given(statusLogRepository.canReturnOrder(order, true)).willReturn(true);
        given(statusLogRepository.getCompletedOrderPaymentAmount(order, true)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.changeOrderStatus(orderNumber, request, xUserId))
                .isInstanceOf(InvalidOrderStatusChangeException.class)
                .hasMessageContaining("반품 가능한 주문이 아닙니다.");

        verify(xUserIdResolver).resolveUserNo(xUserId);
        verify(orderRepository).findByOrderNumber(orderNumber);
        verify(statusLogRepository).canReturnOrder(order, true);
        verify(statusLogRepository).getCompletedOrderPaymentAmount(order, true);
        verify(pointService, never()).processPointRefund(any(), anyLong());
        verify(returnRepository, never()).save(any());
    }

    @Test
    @DisplayName("주문 취소에 성공한다")
    void handleCancelOrder_success() {
        // given
        String orderNumber = "202507-abcdef-123456";
        OrderStatusRequest request = new OrderStatusRequest(
                OrderStatusRequest.OrderAction.CANCEL,
                "취소 사유",
                null
        );
        String xUserId = "testUser";
        Long userNo = 1L;

        Order order = new Order(userNo);
        order.setShippingInfo(shippingInfo);
        order.setStatus(OrderStatus.PENDING_PAY);
        PaymentResDto paymentResDto = new PaymentResDto();
        paymentResDto.setPayAmount(10_000L);
        OrderStatusLog statusLog = new OrderStatusLog(order.getStatus(), OrderStatus.CANCELED, userNo, request.reason(), order);

        given(xUserIdResolver.resolveUserNo(anyString())).willReturn(userNo);
        given(orderRepository.findByOrderNumber(anyString())).willReturn(Optional.of(order));
        given(paymentService.refundCardPaymentByOrderNumber(anyString(), anyString())).willReturn(paymentResDto);
        willDoNothing().given(pointService).processPointRefund(any(Order.class), anyLong());
        given(statusLogRepository.save(any(OrderStatusLog.class))).willReturn(statusLog);

        // when
        OrderStatusResult result = orderService.changeOrderStatus(orderNumber, request, xUserId);

        // then
        assertThat(result).isInstanceOf(OrderStatusResult.CancelResult.class);
        OrderStatusResult.CancelResult cancelResult = (OrderStatusResult.CancelResult) result;
        assertThat(cancelResult.payment().getPayAmount()).isEqualTo(10_000L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);

        verify(xUserIdResolver).resolveUserNo(xUserId);
        verify(orderRepository).findByOrderNumber(orderNumber);
        verify(paymentService).refundCardPaymentByOrderNumber(orderNumber, request.reason());
        verify(pointService).processPointRefund(order, 10_000L);
        verify(statusLogRepository).save(any(OrderStatusLog.class));
    }

    @Test
    @DisplayName("취소 시 존재하지 않는 주문인 경우 OrderNotFoundException 발생")
    void handleCancelOrder_orderNotFound_throwsOrderNotFoundException() {
        // given
        String orderNumber = "INVALID-ORDER";
        String xUserId = "testUser";
        Long userNo = 1L;
        OrderStatusRequest request = new OrderStatusRequest(OrderStatusRequest.OrderAction.CANCEL, "취소 사유", null);

        given(xUserIdResolver.resolveUserNo(xUserId)).willReturn(userNo);
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.changeOrderStatus(orderNumber, request, xUserId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("주문을 찾을 수 없습니다: orderNumber=" + orderNumber);

        verify(xUserIdResolver).resolveUserNo(xUserId);
        verify(orderRepository).findByOrderNumber(orderNumber);
        verify(paymentService, never()).refundCardPaymentByOrderNumber(anyString(), anyString());
    }

    @Test
    @DisplayName("취소 시 본인의 주문이 아닌 경우 NotMemberException 발생")
    void handleCancelOrder_notOwner_throwsNotMemberException() {
        // given
        String orderNumber = "ORDER-123";
        String xUserId = "testUser";
        Long userNo = 1L;
        Long otherUserNo = 2L;
        OrderStatusRequest request = new OrderStatusRequest(OrderStatusRequest.OrderAction.CANCEL, "취소 사유", null);
        Order otherUserOrder = new Order(otherUserNo);

        given(xUserIdResolver.resolveUserNo(xUserId)).willReturn(userNo);
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(otherUserOrder));

        // when & then
        assertThatThrownBy(() -> orderService.changeOrderStatus(orderNumber, request, xUserId))
                .isInstanceOf(NotMemberException.class)
                .hasMessageContaining("본인의 주문만 취소할 수 있습니다.");

        verify(xUserIdResolver).resolveUserNo(xUserId);
        verify(orderRepository).findByOrderNumber(orderNumber);
        verify(paymentService, never()).refundCardPaymentByOrderNumber(anyString(), anyString());
    }

    @Test
    @DisplayName("취소 불가능한 주문 상태에서 취소 시 InvalidOrderStatusChangeException 발생")
    void handleCancelOrder_invalidStatus_throwsInvalidOrderStatusChangeException() {
        // given
        String orderNumber = "ORDER-123";
        String xUserId = "testUser";
        Long userNo = 1L;
        OrderStatusRequest request = new OrderStatusRequest(OrderStatusRequest.OrderAction.CANCEL, "취소 사유", null);
        Order order = new Order(userNo);
        order.setStatus(OrderStatus.COMPLETED); // 취소 불가능한 상태

        given(xUserIdResolver.resolveUserNo(xUserId)).willReturn(userNo);
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> orderService.changeOrderStatus(orderNumber, request, xUserId))
                .isInstanceOf(InvalidOrderStatusChangeException.class)
                .hasMessageContaining("취소할 수 없는 주문 상태입니다: " + OrderStatus.COMPLETED);

        verify(xUserIdResolver).resolveUserNo(xUserId);
        verify(orderRepository).findByOrderNumber(orderNumber);
        verify(paymentService, never()).refundCardPaymentByOrderNumber(anyString(), anyString());
        verify(pointService, never()).processPointRefund(any(), anyLong());
        verify(statusLogRepository, never()).save(any());
    }
}
