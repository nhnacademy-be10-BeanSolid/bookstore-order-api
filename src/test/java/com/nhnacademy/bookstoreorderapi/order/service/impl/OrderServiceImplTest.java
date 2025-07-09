package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookStockReduceRequest;
import com.nhnacademy.bookstoreorderapi.order.client.book.service.BookService;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.UserResponse;
import com.nhnacademy.bookstoreorderapi.order.client.user.service.UserService;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Wrapping;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.BookNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.PurchaseVerificationResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.*;
import com.nhnacademy.bookstoreorderapi.order.service.OrderValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.TaskScheduler;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    OrderItemRepository orderItemRepository;
    @Mock
    CanceledOrderRepository canceledOrderRepository;
    @Mock
    OrderStatusLogRepository statusLogRepository;
    @Mock
    TaskScheduler taskScheduler;
    @Mock
    ReturnsRepository returnRepository;
    @Mock
    BookService bookService;
    @Mock
    UserService userService;
    @Mock
    OrderValidationService orderValidationService;
    @Mock
    CustomOrderRepository customOrderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderRequest validOrderRequest;
    private Map<Long, BookResponse> bookMap;
    private Map<Long, Wrapping> wrappingMap;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        List<OrderRequest.OrderItemRequest> items = List.of(
                new OrderRequest.OrderItemRequest(1L, 2, 15000L, 1L),
                new OrderRequest.OrderItemRequest(2L, 1, 25000L, 2L)
        );

        validOrderRequest = new OrderRequest(
                "홍길동",
                "01012345678",
                "서울특별시 강남구",
                "테헤란로 123",
                "10층",
                LocalDate.now().plusDays(3),
                items
        );

        bookMap = new HashMap<>();
        bookMap.put(1L, BookResponse.builder()
                .id(1L)
                .title("테스트 도서 1")
                .salePrice(15000)
                .stock(10)
                .build());
        bookMap.put(2L, BookResponse.builder()
                .id(2L)
                .title("테스트 도서 2")
                .salePrice(25000)
                .stock(5)
                .build());

        wrappingMap = new HashMap<>();
        Wrapping wrapping1 = new Wrapping();
        wrapping1.setId(1L);
        wrapping1.setName("기본 포장지");
        wrapping1.setPrice(1000);
        
        Wrapping wrapping2 = new Wrapping();
        wrapping2.setId(2L);
        wrapping2.setName("프리미엄 포장지");
        wrapping2.setPrice(3000);
        
        wrappingMap.put(1L, wrapping1);
        wrappingMap.put(2L, wrapping2);

        userResponse = UserResponse.builder()
                .userNo(1L)
                .userId("testUser")
                .build();
    }

    @Test
    @DisplayName("회원 주문 생성에 성공한다")
    void createOrder_withMember_success() {
        // given
        String xUserId = "testUser";
        Order savedOrder = Order.of(validOrderRequest, 1L);
        
        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
        given(orderValidationService.fetchAndValidateBooks(validOrderRequest.orderItems())).willReturn(bookMap);
        given(orderValidationService.fetchAndValidateWrappings(validOrderRequest.orderItems())).willReturn(wrappingMap);
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);
        given(orderItemRepository.saveAll(anyList())).willReturn(List.of());
        
        // when
        OrderResponse result = orderService.createOrder(validOrderRequest, xUserId);
        
        // then
        assertThat(result).isNotNull();
        verify(userService).getUserInfo(xUserId);
        verify(orderValidationService).fetchAndValidateBooks(validOrderRequest.orderItems());
        verify(orderValidationService).fetchAndValidateWrappings(validOrderRequest.orderItems());
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(anyList());
        verify(bookService).stockUpdate(anyList());
    }

    @Test
    @DisplayName("비회원 주문 생성에 성공한다")
    void createOrder_withGuest_success() {
        // given
        String xUserId = null;
        Order savedOrder = Order.of(validOrderRequest, null);
        
        given(orderValidationService.fetchAndValidateBooks(validOrderRequest.orderItems())).willReturn(bookMap);
        given(orderValidationService.fetchAndValidateWrappings(validOrderRequest.orderItems())).willReturn(wrappingMap);
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);
        given(orderItemRepository.saveAll(anyList())).willReturn(List.of());
        
        // when
        OrderResponse result = orderService.createOrder(validOrderRequest, xUserId);
        
        // then
        assertThat(result).isNotNull();
        verify(orderValidationService).fetchAndValidateBooks(validOrderRequest.orderItems());
        verify(orderValidationService).fetchAndValidateWrappings(validOrderRequest.orderItems());
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(anyList());
        verify(bookService).stockUpdate(anyList());
    }

    @Test
    @DisplayName("주문 요청이 null인 경우 예외가 발생한다")
    void createOrder_withNullRequest_throwsException() {
        // given
        String xUserId = "testUser";
        
        // when & then
        assertThatThrownBy(() -> orderService.createOrder(null, xUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("orderRequest는 null일 수 없습니다.");
    }

    @Test
    @DisplayName("도서 검증 실패 시 예외가 발생한다")
    void createOrder_withInvalidBook_throwsException() {
        // given
        String xUserId = "testUser";
        
        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
        given(orderValidationService.fetchAndValidateBooks(validOrderRequest.orderItems()))
                .willThrow(new BookNotFoundException("책을 찾을 수 없습니다."));
        
        // when & then
        assertThatThrownBy(() -> orderService.createOrder(validOrderRequest, xUserId))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("책을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("주문 생성 시 올바른 재고 차감 요청이 전송된다")
    void createOrder_sendsCorrectStockReduceRequest() {
        // given
        String xUserId = "testUser";
        Order savedOrder = Order.of(validOrderRequest, 1L);
        
        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
        given(orderValidationService.fetchAndValidateBooks(validOrderRequest.orderItems())).willReturn(bookMap);
        given(orderValidationService.fetchAndValidateWrappings(validOrderRequest.orderItems())).willReturn(wrappingMap);
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);
        given(orderItemRepository.saveAll(anyList())).willReturn(List.of());
        
        // when
        orderService.createOrder(validOrderRequest, xUserId);
        
        // then
        verify(bookService).stockUpdate(argThat(requests -> {
            List<BookStockReduceRequest> stockRequests = (List<BookStockReduceRequest>) requests;
            return stockRequests.size() == 2 &&
                   stockRequests.stream().anyMatch(req -> req.bookId().equals(1L) && req.stock().equals(2)) &&
                   stockRequests.stream().anyMatch(req -> req.bookId().equals(2L) && req.stock().equals(1));
        }));
    }

    @Test
    @DisplayName("동일한 도서의 여러 주문 아이템이 있을 때 수량이 합산된다")
    void createOrder_withDuplicateBooks_sumsQuantities() {
        // given
        String xUserId = "testUser";
        List<OrderRequest.OrderItemRequest> duplicateItems = List.of(
                new OrderRequest.OrderItemRequest(1L, 2, 15000L, 1L),
                new OrderRequest.OrderItemRequest(1L, 3, 15000L, 1L)
        );
        
        OrderRequest requestWithDuplicates = new OrderRequest(
                "홍길동",
                "01012345678",
                "서울특별시 강남구",
                "테헤란로 123",
                "10층",
                LocalDate.now().plusDays(3),
                duplicateItems
        );
        
        Order savedOrder = Order.of(requestWithDuplicates, 1L);
        
        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
        given(orderValidationService.fetchAndValidateBooks(duplicateItems)).willReturn(bookMap);
        given(orderValidationService.fetchAndValidateWrappings(duplicateItems)).willReturn(wrappingMap);
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);
        given(orderItemRepository.saveAll(anyList())).willReturn(List.of());
        
        // when
        orderService.createOrder(requestWithDuplicates, xUserId);
        
        // then
        verify(bookService).stockUpdate(argThat(requests -> {
            List<BookStockReduceRequest> stockRequests = (List<BookStockReduceRequest>) requests;
            return stockRequests.size() == 1 &&
                   stockRequests.getFirst().bookId().equals(1L) &&
                   stockRequests.getFirst().stock().equals(5);
        }));
    }

    @Test
    @DisplayName("회원 주문 상세 조회에 성공한다")
    void findByOrderId_success() {
        // given
        String xUserId = "testUser";
        String orderId = "ORDER-20240101-001";
        Long userNo = 1L;
        
        Order mockOrder = Order.of(validOrderRequest, userNo);
        
        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
        given(orderRepository.findByOrderIdAndUserNo(orderId, userNo)).willReturn(Optional.of(mockOrder));
        
        // when
        OrderResponse result = orderService.findByOrderId(orderId, xUserId);
        
        // then
        assertThat(result).isNotNull();
        verify(userService).getUserInfo(xUserId);
        verify(orderRepository).findByOrderIdAndUserNo(orderId, userNo);
    }

    @Test
    @DisplayName("회원 주문 상세 조회 시 주문이 존재하지 않으면 예외가 발생한다")
    void findByOrderId_orderNotFound_throwsException() {
        // given
        String xUserId = "testUser";
        String orderId = "ORDER-20240101-001";
        Long userNo = 1L;
        
        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
        given(orderRepository.findByOrderIdAndUserNo(orderId, userNo)).willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> orderService.findByOrderId(orderId, xUserId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("주문을 찾을 수 없습니다. 주문번호: " + orderId);
        
        verify(userService).getUserInfo(xUserId);
        verify(orderRepository).findByOrderIdAndUserNo(orderId, userNo);
    }

    @Test
    @DisplayName("다른 회원의 주문 상세 조회 시 예외가 발생한다")
    void findByOrderId_differentUser_throwsException() {
        // given
        String xUserId = "testUser";
        String orderId = "ORDER-20240101-001";
        Long userNo = 1L;
        
        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
        given(orderRepository.findByOrderIdAndUserNo(orderId, userNo)).willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> orderService.findByOrderId(orderId, xUserId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("주문을 찾을 수 없습니다. 주문번호: " + orderId);
        
        verify(userService).getUserInfo(xUserId);
        verify(orderRepository).findByOrderIdAndUserNo(orderId, userNo);
    }

    @Test
    @DisplayName("xUserId가 null일 때 주문 전체 조회는 불가능하다")
    void findAllByUserId_withNullXUserId_returnsEmpty() {
        // given
        String xUserId = null;
        Page<OrderSummaryResponse> emptyPage = new org.springframework.data.domain.PageImpl<>(List.of());
        
        given(customOrderRepository.findOrderSummary(null, PageRequest.of(0, 10)))
                .willReturn(emptyPage);
        
        // when
        Page<OrderSummaryResponse> result = orderService.findAllByUserId(xUserId);
        
        // then
        assertThat(result.getContent()).isEmpty();
        verify(customOrderRepository).findOrderSummary(null, PageRequest.of(0, 10));
        verify(userService, never()).getUserInfo(anyString());
    }

    @Test
    @DisplayName("xUserId가 blank일 때 주문 전체 조회는 불가능하다")
    void findAllByUserId_withBlankXUserId_returnsEmpty() {
        // given
        String xUserId = "";
        Page<OrderSummaryResponse> emptyPage = new org.springframework.data.domain.PageImpl<>(List.of());
        
        given(customOrderRepository.findOrderSummary(null, PageRequest.of(0, 10)))
                .willReturn(emptyPage);
        
        // when
        Page<OrderSummaryResponse> result = orderService.findAllByUserId(xUserId);
        
        // then
        assertThat(result.getContent()).isEmpty();
        verify(customOrderRepository).findOrderSummary(null, PageRequest.of(0, 10));
        verify(userService, never()).getUserInfo(anyString());
    }

    @Test
    @DisplayName("xUserId가 null일 때 주문 상세 조회는 주문번호만으로 가능하다")
    void findByOrderId_withNullXUserId_success() {
        // given
        String xUserId = null;
        String orderId = "ORDER-20240101-001";
        
        Order mockOrder = Order.of(validOrderRequest, null);
        
        given(orderRepository.findByOrderIdAndUserNo(orderId, null))
                .willReturn(Optional.of(mockOrder));
        
        // when
        OrderResponse result = orderService.findByOrderId(orderId, xUserId);
        
        // then
        assertThat(result).isNotNull();
        verify(orderRepository).findByOrderIdAndUserNo(orderId, null);
        verify(userService, never()).getUserInfo(anyString());
    }

    @Test
    @DisplayName("xUserId가 blank일 때 주문 상세 조회는 주문번호만으로 가능하다")
    void findByOrderId_withBlankXUserId_success() {
        // given
        String xUserId = "";
        String orderId = "ORDER-20240101-001";
        
        Order mockOrder = Order.of(validOrderRequest, null);
        
        given(orderRepository.findByOrderIdAndUserNo(orderId, null))
                .willReturn(Optional.of(mockOrder));
        
        // when
        OrderResponse result = orderService.findByOrderId(orderId, xUserId);
        
        // then
        assertThat(result).isNotNull();
        verify(orderRepository).findByOrderIdAndUserNo(orderId, null);
        verify(userService, never()).getUserInfo(anyString());
    }

    // ========== 구매 확인 테스트 ==========

    @Test
    @DisplayName("구매 확인 성공 - 유효한 사용자와 구매 이력이 있는 경우")
    void verifyPurchase_success_withValidUserAndPurchase() {
        // given
        String xUserId = "testUser";
        Long bookId = 123L;
        Long userNo = 1L;
        
        UserResponse userResponse = UserResponse.builder()
                .userNo(userNo)
                .userId(xUserId)
                .build();
        
        PurchaseVerificationResponse expectedResponse = new PurchaseVerificationResponse(userNo, bookId, true);
        
        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
        given(customOrderRepository.findByUserNoAndBookId(userNo, bookId)).willReturn(expectedResponse);

        // when
        PurchaseVerificationResponse result = orderService.verifyPurchase(xUserId, bookId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserNo()).isEqualTo(userNo);
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getIsValid()).isTrue();
        
        verify(userService).getUserInfo(xUserId);
        verify(customOrderRepository).findByUserNoAndBookId(userNo, bookId);
    }

    @Test
    @DisplayName("구매 확인 성공 - 유효한 사용자이지만 구매 이력이 없는 경우")
    void verifyPurchase_success_withValidUserButNoPurchase() {
        // given
        String xUserId = "testUser";
        Long bookId = 456L;
        Long userNo = 2L;
        
        UserResponse userResponse = UserResponse.builder()
                .userNo(userNo)
                .userId(xUserId)
                .build();
        
        PurchaseVerificationResponse expectedResponse = new PurchaseVerificationResponse(userNo, bookId, false);
        
        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
        given(customOrderRepository.findByUserNoAndBookId(userNo, bookId)).willReturn(expectedResponse);

        // when
        PurchaseVerificationResponse result = orderService.verifyPurchase(xUserId, bookId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserNo()).isEqualTo(userNo);
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getIsValid()).isFalse();
        
        verify(userService).getUserInfo(xUserId);
        verify(customOrderRepository).findByUserNoAndBookId(userNo, bookId);
    }

    @Test
    @DisplayName("구매 확인 - null 사용자 ID로 인한 예외 발생")
    void verifyPurchase_withNullUserId_throwsException() {
        // given
        String xUserId = null;
        Long bookId = 789L;

        // when & then
        assertThatThrownBy(() -> orderService.verifyPurchase(xUserId, bookId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("구매 검증에 필요한 정보(회원 정보 혹은 도서 정보)가 빠져있습니다.");
        
        verify(userService, never()).getUserInfo(anyString());
        verify(customOrderRepository, never()).findByUserNoAndBookId(any(), any());
    }

    @Test
    @DisplayName("구매 확인 - 빈 문자열 사용자 ID로 인한 예외 발생")
    void verifyPurchase_withBlankUserId_throwsException() {
        // given
        String xUserId = "";
        Long bookId = 101L;

        // when & then
        assertThatThrownBy(() -> orderService.verifyPurchase(xUserId, bookId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("구매 검증에 필요한 정보(회원 정보 혹은 도서 정보)가 빠져있습니다.");
        
        verify(userService, never()).getUserInfo(anyString());
        verify(customOrderRepository, never()).findByUserNoAndBookId(any(), any());
    }

    @Test
    @DisplayName("구매 확인 - null bookId로 인한 예외 발생")
    void verifyPurchase_withNullBookId_throwsException() {
        // given
        String xUserId = "testUser";
        Long bookId = null;
        
        UserResponse userResponse = UserResponse.builder()
                .userNo(1L)
                .userId(xUserId)
                .build();
        
        given(userService.getUserInfo(xUserId)).willReturn(userResponse);

        // when & then
        assertThatThrownBy(() -> orderService.verifyPurchase(xUserId, bookId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("구매 검증에 필요한 정보(회원 정보 혹은 도서 정보)가 빠져있습니다.");
        
        verify(userService).getUserInfo(xUserId);
        verify(customOrderRepository, never()).findByUserNoAndBookId(any(), any());
    }

    @Test
    @DisplayName("구매 확인 - 공백만 있는 사용자 ID로 인한 예외 발생")
    void verifyPurchase_withWhitespaceUserId_throwsException() {
        // given
        String xUserId = "   ";
        Long bookId = 202L;

        // when & then
        assertThatThrownBy(() -> orderService.verifyPurchase(xUserId, bookId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("구매 검증에 필요한 정보(회원 정보 혹은 도서 정보)가 빠져있습니다.");
        
        verify(userService, never()).getUserInfo(anyString());
        verify(customOrderRepository, never()).findByUserNoAndBookId(any(), any());
    }

    @Test
    @DisplayName("구매 확인 - 사용자 서비스에서 사용자 정보 조회")
    void verifyPurchase_userServiceInteraction() {
        // given
        String xUserId = "activeUser";
        Long bookId = 999L;
        Long userNo = 5L;
        
        UserResponse userResponse = UserResponse.builder()
                .userNo(userNo)
                .userId(xUserId)
                .userName("홍길동")
                .userEmail("test@example.com")
                .build();
        
        PurchaseVerificationResponse expectedResponse = new PurchaseVerificationResponse(userNo, bookId, true);
        
        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
        given(customOrderRepository.findByUserNoAndBookId(userNo, bookId)).willReturn(expectedResponse);

        // when
        PurchaseVerificationResponse result = orderService.verifyPurchase(xUserId, bookId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserNo()).isEqualTo(userNo);
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getIsValid()).isTrue();
        
        verify(userService).getUserInfo(xUserId);
        verify(customOrderRepository).findByUserNoAndBookId(userNo, bookId);
    }

    @Test
    @DisplayName("구매 확인 - 다양한 bookId 값 처리")
    void verifyPurchase_withVariousBookIds() {
        // given
        String xUserId = "testUser";
        Long bookId = Long.MAX_VALUE; // Edge case: 매우 큰 bookId
        Long userNo = 3L;
        
        UserResponse userResponse = UserResponse.builder()
                .userNo(userNo)
                .userId(xUserId)
                .build();
        
        PurchaseVerificationResponse expectedResponse = new PurchaseVerificationResponse(userNo, bookId, false);
        
        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
        given(customOrderRepository.findByUserNoAndBookId(userNo, bookId)).willReturn(expectedResponse);

        // when
        PurchaseVerificationResponse result = orderService.verifyPurchase(xUserId, bookId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserNo()).isEqualTo(userNo);
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getIsValid()).isFalse();
        
        verify(userService).getUserInfo(xUserId);
        verify(customOrderRepository).findByUserNoAndBookId(userNo, bookId);
    }
}
