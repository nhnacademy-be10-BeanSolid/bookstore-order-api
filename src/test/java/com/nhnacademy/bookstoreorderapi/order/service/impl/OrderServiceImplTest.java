package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.client.book.service.BookService;
import com.nhnacademy.bookstoreorderapi.order.common.resolver.XUserIdResolver;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderItem;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Wrapping;
import com.nhnacademy.bookstoreorderapi.order.dto.request.CreateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.UpdateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.CreateOrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderItemRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.WrappingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    private BookService bookService;
    @Mock
    private XUserIdResolver xUserIdResolver;
//    @Mock
//    OrderValidationService orderValidationService;
//    @Mock
//    CustomOrderRepository customOrderRepository;
    @InjectMocks
    private OrderServiceImpl orderService;

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
        given(bookService.getBookOrderResponse(anyList())).willReturn(List.of(BookResponse.builder().id(1L).build()));

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
        given(bookService.getBookOrderResponse(anyList())).willReturn(List.of(BookResponse.builder().id(1L).build()));

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
        given(bookService.getBookOrderResponse(anyList())).willReturn(List.of(BookResponse.builder().id(1L).build()));

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
        given(bookService.getBookOrderResponse(anyList())).willReturn(List.of(BookResponse.builder().id(1L).build()));

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
//
//    @Test
//    @DisplayName("회원 주문 생성에 성공한다")
//    void createOrder_withMember_success() {
//        // given
//        String xUserId = "testUser";
//        Order savedOrder = Order.of(validOrderRequest, 1L);
//
//        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
//        given(orderValidationService.fetchAndValidateBooks(validOrderRequest.orderItems())).willReturn(bookMap);
//        given(orderValidationService.fetchAndValidateWrappings(validOrderRequest.orderItems())).willReturn(wrappingMap);
//        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);
//        given(orderItemRepository.saveAll(anyList())).willReturn(List.of());
//
//        // when
//        OrderResponse result = orderService.createOrder(validOrderRequest, xUserId);
//
//        // then
//        assertThat(result).isNotNull();
//        verify(userService).getUserInfo(xUserId);
//        verify(orderValidationService).fetchAndValidateBooks(validOrderRequest.orderItems());
//        verify(orderValidationService).fetchAndValidateWrappings(validOrderRequest.orderItems());
//        verify(orderRepository).save(any(Order.class));
//        verify(orderItemRepository).saveAll(anyList());
//        verify(bookService).stockUpdate(anyList());
//    }
//
//    @Test
//    @DisplayName("비회원 주문 생성에 성공한다")
//    void createOrder_withGuest_success() {
//        // given
//        String xUserId = null;
//        Order savedOrder = Order.of(validOrderRequest, null);
//
//        given(orderValidationService.fetchAndValidateBooks(validOrderRequest.orderItems())).willReturn(bookMap);
//        given(orderValidationService.fetchAndValidateWrappings(validOrderRequest.orderItems())).willReturn(wrappingMap);
//        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);
//        given(orderItemRepository.saveAll(anyList())).willReturn(List.of());
//
//        // when
//        OrderResponse result = orderService.createOrder(validOrderRequest, xUserId);
//
//        // then
//        assertThat(result).isNotNull();
//        verify(orderValidationService).fetchAndValidateBooks(validOrderRequest.orderItems());
//        verify(orderValidationService).fetchAndValidateWrappings(validOrderRequest.orderItems());
//        verify(orderRepository).save(any(Order.class));
//        verify(orderItemRepository).saveAll(anyList());
//        verify(bookService).stockUpdate(anyList());
//    }
//
//    @Test
//    @DisplayName("주문 요청이 null인 경우 예외가 발생한다")
//    void createOrder_withNullRequest_throwsException() {
//        // given
//        String xUserId = "testUser";
//
//        // when & then
//        assertThatThrownBy(() -> orderService.createOrder(null, xUserId))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage("orderRequest는 null일 수 없습니다.");
//    }
//
//    @Test
//    @DisplayName("도서 검증 실패 시 예외가 발생한다")
//    void createOrder_withInvalidBook_throwsException() {
//        // given
//        String xUserId = "testUser";
//
//        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
//        given(orderValidationService.fetchAndValidateBooks(validOrderRequest.orderItems()))
//                .willThrow(new BookNotFoundException("책을 찾을 수 없습니다."));
//
//        // when & then
//        assertThatThrownBy(() -> orderService.createOrder(validOrderRequest, xUserId))
//                .isInstanceOf(BookNotFoundException.class)
//                .hasMessage("책을 찾을 수 없습니다.");
//    }
//
//    @Test
//    @DisplayName("주문 생성 시 올바른 재고 차감 요청이 전송된다")
//    void createOrder_sendsCorrectStockReduceRequest() {
//        // given
//        String xUserId = "testUser";
//        Order savedOrder = Order.of(validOrderRequest, 1L);
//
//        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
//        given(orderValidationService.fetchAndValidateBooks(validOrderRequest.orderItems())).willReturn(bookMap);
//        given(orderValidationService.fetchAndValidateWrappings(validOrderRequest.orderItems())).willReturn(wrappingMap);
//        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);
//        given(orderItemRepository.saveAll(anyList())).willReturn(List.of());
//
//        // when
//        orderService.createOrder(validOrderRequest, xUserId);
//
//        // then
//        verify(bookService).stockUpdate(argThat(requests -> {
//            List<BookStockReduceRequest> stockRequests = (List<BookStockReduceRequest>) requests;
//            return stockRequests.size() == 2 &&
//                   stockRequests.stream().anyMatch(req -> req.bookId().equals(1L) && req.stock().equals(2)) &&
//                   stockRequests.stream().anyMatch(req -> req.bookId().equals(2L) && req.stock().equals(1));
//        }));
//    }
//
//    @Test
//    @DisplayName("동일한 도서의 여러 주문 아이템이 있을 때 수량이 합산된다")
//    void createOrder_withDuplicateBooks_sumsQuantities() {
//        // given
//        String xUserId = "testUser";
//        List<OrderRequest.OrderItemRequest> duplicateItems = List.of(
//                new OrderRequest.OrderItemRequest(1L, 2, 15000L, 1L),
//                new OrderRequest.OrderItemRequest(1L, 3, 15000L, 1L)
//        );
//
//        OrderRequest requestWithDuplicates = new OrderRequest(
//                "홍길동",
//                "010-1234-5678",
//                "12345 서울특별시 강남구 테헤란로 123 10층",
//                LocalDate.now().plusDays(3),
//                duplicateItems
//        );
//
//        Order savedOrder = Order.of(requestWithDuplicates, 1L);
//
//        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
//        given(orderValidationService.fetchAndValidateBooks(duplicateItems)).willReturn(bookMap);
//        given(orderValidationService.fetchAndValidateWrappings(duplicateItems)).willReturn(wrappingMap);
//        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);
//        given(orderItemRepository.saveAll(anyList())).willReturn(List.of());
//
//        // when
//        orderService.createOrder(requestWithDuplicates, xUserId);
//
//        // then
//        verify(bookService).stockUpdate(argThat(requests -> {
//            List<BookStockReduceRequest> stockRequests = (List<BookStockReduceRequest>) requests;
//            return stockRequests.size() == 1 &&
//                   stockRequests.getFirst().bookId().equals(1L) &&
//                   stockRequests.getFirst().stock().equals(5);
//        }));
//    }
//
//    @Test
//    @DisplayName("회원 주문 상세 조회에 성공한다")
//    void findByOrderId_success() {
//        // given
//        String xUserId = "testUser";
//        String orderId = "202507-abcabc-123123";
//        Long userNo = 1L;
//
//        Order mockOrder = Order.of(validOrderRequest, userNo);
//        mockOrder.setStatus(OrderStatus.PENDING);
//
//        given(xUserIdResolver.resolveUserNo(xUserId)).willReturn(userNo);
//        given(orderRepository.findByOrderIdAndUserNo(orderId, userNo)).willReturn(Optional.of(mockOrder));
//
//        // when
//        OrderDetailResponse result = orderService.findByOrderId(xUserId, orderId);
//
//        // then
//        assertThat(result).isNotNull();
//        verify(xUserIdResolver).resolveUserNo(xUserId);
//        verify(orderRepository).findByOrderIdAndUserNo(orderId, userNo);
//    }
//
//    @Test
//    @DisplayName("회원 주문 상세 조회 시 주문이 존재하지 않으면 예외가 발생한다")
//    void findByOrderId_orderNotFound_throwsException() {
//        // given
//        String xUserId = "testUser";
//        String orderId = "202507-abcabc-123123";
//        Long userNo = -1L;
//
//        given(xUserIdResolver.resolveUserNo(xUserId)).willReturn(userNo);
//        given(orderRepository.findByOrderIdAndUserNo(orderId, userNo)).willReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> orderService.findByOrderId(xUserId, orderId))
//                .isInstanceOf(OrderNotFoundException.class)
//                .hasMessage("주문을 찾을 수 없습니다: orderId=" + orderId);
//    }
//
//    @Test
//    @DisplayName("다른 회원의 주문 상세 조회 시 예외가 발생한다")
//    void findByOrderId_differentUser_throwsException() {
//        // given
//        String xUserId = "testUser";
//        String orderId = "202507-abcabc-123123";
//        Long userNo = -1L;
//
//        given(xUserIdResolver.resolveUserNo(xUserId)).willReturn(userNo);
//        given(orderRepository.findByOrderIdAndUserNo(orderId, userNo)).willReturn(Optional.empty());
//
//        // when & then
//        assertThatThrownBy(() -> orderService.findByOrderId(xUserId, orderId))
//                .isInstanceOf(OrderNotFoundException.class)
//                .hasMessage("주문을 찾을 수 없습니다: orderId=" + orderId);
//    }
//
//    @Test
//    @DisplayName("xUserId가 null일 때 주문 전체 조회는 불가능하다")
//    void findAllByUserId_withNullXUserId_returnsEmpty() {
//        // given
//        String xUserId = null;
//        Page<OrderSummaryResponse> emptyPage = new org.springframework.data.domain.PageImpl<>(List.of());
//
//        given(customOrderRepository.findOrderSummary(null, PageRequest.of(0, 20)))
//                .willReturn(emptyPage);
//
//        // when
//        Page<OrderSummaryResponse> result = orderService.findAllByUserId(xUserId, PageRequest.of(0, 20));
//
//        // then
//        assertThat(result.getContent()).isEmpty();
//        verify(customOrderRepository).findOrderSummary(null, PageRequest.of(0, 20));
//        verify(userService, never()).getUserInfo(anyString());
//    }
//
//    @Test
//    @DisplayName("xUserId가 blank일 때 주문 전체 조회는 불가능하다")
//    void findAllByUserId_withBlankXUserId_returnsEmpty() {
//        // given
//        String xUserId = "";
//        Page<OrderSummaryResponse> emptyPage = new org.springframework.data.domain.PageImpl<>(List.of());
//
//        given(customOrderRepository.findOrderSummary(null, PageRequest.of(0, 20)))
//                .willReturn(emptyPage);
//
//        // when
//        Page<OrderSummaryResponse> result = orderService.findAllByUserId(xUserId, PageRequest.of(0, 20));
//
//        // then
//        assertThat(result.getContent()).isEmpty();
//        verify(customOrderRepository).findOrderSummary(null, PageRequest.of(0, 20));
//        verify(userService, never()).getUserInfo(anyString());
//    }
//
//    @Test
//    @DisplayName("xUserId가 null일 때 주문 상세 조회는 주문번호만으로 가능하다")
//    void findByOrderId_withNullXUserId_success() {
//        // given
//        String xUserId = null;
//        String orderId = "202507-abcabc-123123";
//
//        Order mockOrder = Order.of(validOrderRequest, null);
//        mockOrder.setStatus(OrderStatus.PENDING);
//
//        given(xUserIdResolver.resolveUserNo(xUserId)).willReturn(0L);
//        given(orderRepository.findByOrderIdAndUserNo(orderId, 0L))
//                .willReturn(Optional.of(mockOrder));
//
//        // when
//        OrderDetailResponse result = orderService.findByOrderId(xUserId, orderId);
//
//        // then
//        assertThat(result).isNotNull();
//        verify(orderRepository).findByOrderIdAndUserNo(orderId, 0L);
//        verify(userService, never()).getUserInfo(anyString());
//    }
//
//    @Test
//    @DisplayName("xUserId가 blank일 때 주문 상세 조회는 주문번호만으로 가능하다")
//    void findByOrderId_withBlankXUserId_success() {
//        // given
//        String xUserId = "";
//        String orderId = "202507-abcabc-123123";
//
//        Order mockOrder = Order.of(validOrderRequest, null);
//        mockOrder.setStatus(OrderStatus.PENDING);
//
//        given(xUserIdResolver.resolveUserNo(xUserId)).willReturn(0L);
//        given(orderRepository.findByOrderIdAndUserNo(orderId, 0L))
//                .willReturn(Optional.of(mockOrder));
//
//        // when
//        OrderDetailResponse result = orderService.findByOrderId(xUserId, orderId);
//
//        // then
//        assertThat(result).isNotNull();
//        verify(orderRepository).findByOrderIdAndUserNo(orderId, 0L);
//        verify(userService, never()).getUserInfo(anyString());
//    }
//
//    // ========== 구매 확인 테스트 ==========
//
//    @Test
//    @DisplayName("구매 확인 성공 - 유효한 사용자와 구매 이력이 있는 경우")
//    void verifyPurchase_success_withValidUserAndPurchase() {
//        // given
//        String xUserId = "testUser";
//        Long bookId = 123L;
//        Long userNo = 1L;
//
//        UserResponse userResponse = UserResponse.builder()
//                .userNo(userNo)
//                .userId(xUserId)
//                .build();
//
//        PurchaseVerificationResponse expectedResponse = new PurchaseVerificationResponse(userNo, bookId, true);
//
//        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
//        given(customOrderRepository.findByUserNoAndBookId(userNo, bookId)).willReturn(expectedResponse);
//
//        // when
//        PurchaseVerificationResponse result = orderService.verifyPurchase(xUserId, bookId);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getUserNo()).isEqualTo(userNo);
//        assertThat(result.getBookId()).isEqualTo(bookId);
//        assertThat(result.getIsValid()).isTrue();
//
//        verify(userService).getUserInfo(xUserId);
//        verify(customOrderRepository).findByUserNoAndBookId(userNo, bookId);
//    }
//
//    @Test
//    @DisplayName("구매 확인 성공 - 유효한 사용자이지만 구매 이력이 없는 경우")
//    void verifyPurchase_success_withValidUserButNoPurchase() {
//        // given
//        String xUserId = "testUser";
//        Long bookId = 456L;
//        Long userNo = 2L;
//
//        UserResponse userResponse = UserResponse.builder()
//                .userNo(userNo)
//                .userId(xUserId)
//                .build();
//
//        PurchaseVerificationResponse expectedResponse = new PurchaseVerificationResponse(userNo, bookId, false);
//
//        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
//        given(customOrderRepository.findByUserNoAndBookId(userNo, bookId)).willReturn(expectedResponse);
//
//        // when
//        PurchaseVerificationResponse result = orderService.verifyPurchase(xUserId, bookId);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getUserNo()).isEqualTo(userNo);
//        assertThat(result.getBookId()).isEqualTo(bookId);
//        assertThat(result.getIsValid()).isFalse();
//
//        verify(userService).getUserInfo(xUserId);
//        verify(customOrderRepository).findByUserNoAndBookId(userNo, bookId);
//    }
//
//    @Test
//    @DisplayName("구매 확인 - null 사용자 ID로 인한 예외 발생")
//    void verifyPurchase_withNullUserId_throwsException() {
//        // given
//        String xUserId = null;
//        Long bookId = 789L;
//
//        // when & then
//        assertThatThrownBy(() -> orderService.verifyPurchase(xUserId, bookId))
//                .isInstanceOf(MissingRequiredParameterException.class)
//                .hasMessage("구매 검증에 필요한 정보(회원 정보 혹은 도서 정보)가 빠져있습니다.");
//
//        verify(userService, never()).getUserInfo(anyString());
//        verify(customOrderRepository, never()).findByUserNoAndBookId(any(), any());
//    }
//
//    @Test
//    @DisplayName("구매 확인 - 빈 문자열 사용자 ID로 인한 예외 발생")
//    void verifyPurchase_withBlankUserId_throwsException() {
//        // given
//        String xUserId = "";
//        Long bookId = 101L;
//
//        // when & then
//        assertThatThrownBy(() -> orderService.verifyPurchase(xUserId, bookId))
//                .isInstanceOf(MissingRequiredParameterException.class)
//                .hasMessage("구매 검증에 필요한 정보(회원 정보 혹은 도서 정보)가 빠져있습니다.");
//
//        verify(userService, never()).getUserInfo(anyString());
//        verify(customOrderRepository, never()).findByUserNoAndBookId(any(), any());
//    }
//
//    @Test
//    @DisplayName("구매 확인 - null bookId로 인한 예외 발생")
//    void verifyPurchase_withNullBookId_throwsException() {
//        // given
//        String xUserId = "testUser";
//        Long bookId = null;
//
//        UserResponse userResponse = UserResponse.builder()
//                .userNo(1L)
//                .userId(xUserId)
//                .build();
//
//        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
//
//        // when & then
//        assertThatThrownBy(() -> orderService.verifyPurchase(xUserId, bookId))
//                .isInstanceOf(MissingRequiredParameterException.class)
//                .hasMessage("구매 검증에 필요한 정보(회원 정보 혹은 도서 정보)가 빠져있습니다.");
//
//        verify(userService).getUserInfo(xUserId);
//        verify(customOrderRepository, never()).findByUserNoAndBookId(any(), any());
//    }
//
//    @Test
//    @DisplayName("구매 확인 - 공백만 있는 사용자 ID로 인한 예외 발생")
//    void verifyPurchase_withWhitespaceUserId_throwsException() {
//        // given
//        String xUserId = "   ";
//        Long bookId = 202L;
//
//        // when & then
//        assertThatThrownBy(() -> orderService.verifyPurchase(xUserId, bookId))
//                .isInstanceOf(MissingRequiredParameterException.class)
//                .hasMessage("구매 검증에 필요한 정보(회원 정보 혹은 도서 정보)가 빠져있습니다.");
//
//        verify(userService, never()).getUserInfo(anyString());
//        verify(customOrderRepository, never()).findByUserNoAndBookId(any(), any());
//    }
//
//    @Test
//    @DisplayName("구매 확인 - 사용자 서비스에서 사용자 정보 조회")
//    void verifyPurchase_userServiceInteraction() {
//        // given
//        String xUserId = "activeUser";
//        Long bookId = 999L;
//        Long userNo = 5L;
//
//        UserResponse userResponse = UserResponse.builder()
//                .userNo(userNo)
//                .userId(xUserId)
//                .userName("홍길동")
//                .userEmail("test@example.com")
//                .build();
//
//        PurchaseVerificationResponse expectedResponse = new PurchaseVerificationResponse(userNo, bookId, true);
//
//        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
//        given(customOrderRepository.findByUserNoAndBookId(userNo, bookId)).willReturn(expectedResponse);
//
//        // when
//        PurchaseVerificationResponse result = orderService.verifyPurchase(xUserId, bookId);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getUserNo()).isEqualTo(userNo);
//        assertThat(result.getBookId()).isEqualTo(bookId);
//        assertThat(result.getIsValid()).isTrue();
//
//        verify(userService).getUserInfo(xUserId);
//        verify(customOrderRepository).findByUserNoAndBookId(userNo, bookId);
//    }
//
//    @Test
//    @DisplayName("구매 확인 - 다양한 bookId 값 처리")
//    void verifyPurchase_withVariousBookIds() {
//        // given
//        String xUserId = "testUser";
//        Long bookId = Long.MAX_VALUE; // Edge case: 매우 큰 bookId
//        Long userNo = 3L;
//
//        UserResponse userResponse = UserResponse.builder()
//                .userNo(userNo)
//                .userId(xUserId)
//                .build();
//
//        PurchaseVerificationResponse expectedResponse = new PurchaseVerificationResponse(userNo, bookId, false);
//
//        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
//        given(customOrderRepository.findByUserNoAndBookId(userNo, bookId)).willReturn(expectedResponse);
//
//        // when
//        PurchaseVerificationResponse result = orderService.verifyPurchase(xUserId, bookId);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getUserNo()).isEqualTo(userNo);
//        assertThat(result.getBookId()).isEqualTo(bookId);
//        assertThat(result.getIsValid()).isFalse();
//
//        verify(userService).getUserInfo(xUserId);
//        verify(customOrderRepository).findByUserNoAndBookId(userNo, bookId);
//    }
}
