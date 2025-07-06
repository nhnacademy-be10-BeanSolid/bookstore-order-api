package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookStockReduceRequest;
import com.nhnacademy.bookstoreorderapi.order.client.book.service.BookService;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.UserResponse;
import com.nhnacademy.bookstoreorderapi.order.client.user.service.UserService;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Wrapping;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.BookNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.*;
import com.nhnacademy.bookstoreorderapi.order.service.OrderValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
