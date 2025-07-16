package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.common.resolver.XUserIdResolver;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatusLog;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.ShippingInfo;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderStatusLogRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderAdminServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderStatusLogRepository statusLogRepository;
    @Mock
    private XUserIdResolver xUserIdResolver;
    @InjectMocks
    private OrderAdminServiceImpl orderAdminService;

    @Test
    @DisplayName("전체 주문 조회에 성공한다")
    void getAllOrders_success() {
        // given
        String xUserId = "testUser";
        Pageable pageable = PageRequest.of(0, 1);
        List<OrderSummaryResponse> responses = List.of(
                new OrderSummaryResponse(LocalDate.now(), "202507-abcdef-123456", "받는 사람", 100L, "PENDING"),
                new OrderSummaryResponse(LocalDate.now(), "202508-abcdef-123456", "받는 사람", 100L, "SHIPPING"));
        Page<OrderSummaryResponse> responsePage = new PageImpl<>(responses, pageable, responses.size());

        given(orderRepository.findAllOrderSummary(any())).willReturn(responsePage);

        // when
        Page<OrderSummaryResponse> result = orderAdminService.getAllOrders(pageable, xUserId);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).isEqualTo(responses);

        verify(orderRepository, times(1)).findAllOrderSummary(any());
    }

    @Test
    @DisplayName("주문상태 변경(대기 -> 배송)에 성공한다")
    void changeStatusToShipping_success() {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = "admin";

        Long createdBy = 99L;
        Order order = new Order(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingInfo(new ShippingInfo("받는 사람", "010-1234-5678", "주소", LocalDate.now(), 3_000));

        OrderStatusLog statusLog = new OrderStatusLog(order.getStatus(), OrderStatus.SHIPPING, createdBy, null, order);

        given(orderRepository.findByOrderNumber(anyString())).willReturn(Optional.of(order));
        given(statusLogRepository.save(any(OrderStatusLog.class))).willReturn(statusLog);

        // when
        OrderResponse result = orderAdminService.changeStatusToShipping(orderNumber, xUserId);

        // then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPING.name());

        verify(orderRepository, times(1)).findByOrderNumber(anyString());
        verify(statusLogRepository, times(1)).save(any(OrderStatusLog.class));
    }

    @Test
    @DisplayName("주문번호와 일치하는 주문이 없으면 주문상태 변경(대기 -> 배송)에 실패한다")
    void changeStatusToShipping_orderNotFound_fail() {
        // given
        String orderNumber = "unKnown";
        String xUserId = "admin";

        given(orderRepository.findByOrderNumber(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderAdminService.changeStatusToShipping(orderNumber, xUserId))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, times(1)).findByOrderNumber(anyString());
        verify(statusLogRepository, never()).save(any(OrderStatusLog.class));
    }
}