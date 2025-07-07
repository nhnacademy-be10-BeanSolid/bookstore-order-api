package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.client.user.dto.UserResponse;
import com.nhnacademy.bookstoreorderapi.order.client.user.service.UserService;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.CustomOrderRepository;
import com.nhnacademy.bookstoreorderapi.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceFindAllTest {

    @Mock
    private CustomOrderRepository customOrderRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("회원 주문 전체 조회에 성공한다")
    void findAllByUserId_success() {
        // given
        String xUserId = "testUser";
        Long userNo = 123L;
        UserResponse userResponse = new UserResponse(
                userNo, 
                "testUser", 
                "testPassword", 
                "홍길동", 
                "01012345678", 
                "test@example.com", 
                LocalDate.of(1990, 1, 1), 
                1000, 
                false, 
                "ACTIVE", 
                LocalDateTime.now(), 
                LocalDateTime.now(), 
                "BRONZE"
        );
        
        List<OrderSummaryResponse> orderSummaries = List.of(
                new OrderSummaryResponse(LocalDate.now(), "ORDER-20240101-001", "홍길동", 10_000L),
                new OrderSummaryResponse(LocalDate.now().minusDays(1), "ORDER-20240101-002", "김철수", 10_000L)
        );
        Page<OrderSummaryResponse> expectedPage = new PageImpl<>(orderSummaries);
        
        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
        given(customOrderRepository.findOrderSummary(eq(userNo), any(Pageable.class))).willReturn(expectedPage);

        // when
        Page<OrderSummaryResponse> result = orderService.findAllByUserId(xUserId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).orderId()).isEqualTo("ORDER-20240101-001");
        assertThat(result.getContent().get(0).receiverName()).isEqualTo("홍길동");
        assertThat(result.getContent().get(1).orderId()).isEqualTo("ORDER-20240101-002");
        assertThat(result.getContent().get(1).receiverName()).isEqualTo("김철수");
        
        verify(userService).getUserInfo(xUserId);
        verify(customOrderRepository).findOrderSummary(eq(userNo), eq(PageRequest.of(0, 10)));
    }

    @Test
    @DisplayName("회원 주문 전체 조회 시 빈 페이지를 반환한다")
    void findAllByUserId_emptyPage() {
        // given
        String xUserId = "testUser";
        Long userNo = 123L;
        UserResponse userResponse = new UserResponse(
                userNo, 
                "testUser", 
                "testPassword", 
                "홍길동", 
                "01012345678", 
                "test@example.com", 
                LocalDate.of(1990, 1, 1), 
                1000, 
                false, 
                "ACTIVE", 
                LocalDateTime.now(), 
                LocalDateTime.now(), 
                "BRONZE"
        );
        
        Page<OrderSummaryResponse> emptyPage = new PageImpl<>(List.of());
        
        given(userService.getUserInfo(xUserId)).willReturn(userResponse);
        given(customOrderRepository.findOrderSummary(eq(userNo), any(Pageable.class))).willReturn(emptyPage);

        // when
        Page<OrderSummaryResponse> result = orderService.findAllByUserId(xUserId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        
        verify(userService).getUserInfo(xUserId);
        verify(customOrderRepository).findOrderSummary(eq(userNo), eq(PageRequest.of(0, 10)));
    }

    @Test
    @DisplayName("빈 xUserId로 요청하면 userNo가 null이 된다")
    void findAllByUserId_emptyXUserId() {
        // given
        String xUserId = "";
        Page<OrderSummaryResponse> emptyPage = new PageImpl<>(List.of());
        
        given(customOrderRepository.findOrderSummary(eq(null), any(Pageable.class))).willReturn(emptyPage);

        // when
        Page<OrderSummaryResponse> result = orderService.findAllByUserId(xUserId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        
        verify(customOrderRepository).findOrderSummary(eq(null), eq(PageRequest.of(0, 10)));
    }

    @Test
    @DisplayName("null xUserId로 요청하면 userNo가 null이 된다")
    void findAllByUserId_nullXUserId() {
        // given
        String xUserId = null;
        Page<OrderSummaryResponse> emptyPage = new PageImpl<>(List.of());
        
        given(customOrderRepository.findOrderSummary(eq(null), any(Pageable.class))).willReturn(emptyPage);

        // when
        Page<OrderSummaryResponse> result = orderService.findAllByUserId(xUserId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        
        verify(customOrderRepository).findOrderSummary(eq(null), eq(PageRequest.of(0, 10)));
    }
}