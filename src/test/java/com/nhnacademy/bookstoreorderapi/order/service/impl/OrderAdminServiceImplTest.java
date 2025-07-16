package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.common.resolver.XUserIdResolver;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatusLog;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.ShippingInfo;
import com.nhnacademy.bookstoreorderapi.order.dto.internal.ScheduledOrderCompletion;
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

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
        given(xUserIdResolver.isAdmin(anyString())).willReturn(true);

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
        given(xUserIdResolver.isAdmin(anyString())).willReturn(true);

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
        given(xUserIdResolver.isAdmin(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> orderAdminService.changeStatusToShipping(orderNumber, xUserId))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, times(1)).findByOrderNumber(anyString());
        verify(statusLogRepository, never()).save(any(OrderStatusLog.class));
    }

    @Test
    @DisplayName("주문 자동 완료 스케줄링이 정상적으로 등록된다")
    void scheduleOrderCompletion_success() throws Exception {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = "admin";
        Long createdBy = 99L;
        
        Order order = new Order(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingInfo(new ShippingInfo("받는 사람", "010-1234-5678", "주소", LocalDate.now(), 3_000));

        given(orderRepository.findByOrderNumber(anyString())).willReturn(Optional.of(order));
        given(xUserIdResolver.resolveUserNo(anyString())).willReturn(createdBy);
        given(xUserIdResolver.isAdmin(anyString())).willReturn(true);

        // when
        orderAdminService.changeStatusToShipping(orderNumber, xUserId);

        // then
        Field field = OrderAdminServiceImpl.class.getDeclaredField("scheduledCompletions");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, ScheduledOrderCompletion> scheduledCompletions = 
            (Map<String, ScheduledOrderCompletion>) field.get(orderAdminService);
        
        assertThat(scheduledCompletions).containsKey(orderNumber);
        ScheduledOrderCompletion completion = scheduledCompletions.get(orderNumber);
        assertThat(completion.orderNumber()).isEqualTo(orderNumber);
        assertThat(completion.createdBy()).isEqualTo(createdBy);
        assertThat(completion.completionTime()).isAfter(LocalDateTime.now().plusSeconds(5));
    }

    @Test
    @DisplayName("스케줄된 주문 자동 완료가 정상적으로 처리된다")
    void processScheduledCompletions_success() throws Exception {
        // given
        String orderNumber = "202507-abcdef-123456";
        Long createdBy = 99L;
        LocalDateTime pastTime = LocalDateTime.now().minusSeconds(1);
        
        Order order = new Order(1L);
        order.setStatus(OrderStatus.SHIPPING);
        order.setShippingInfo(new ShippingInfo("받는 사람", "010-1234-5678", "주소", LocalDate.now(), 3_000));

        Field field = OrderAdminServiceImpl.class.getDeclaredField("scheduledCompletions");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, ScheduledOrderCompletion> scheduledCompletions = 
            (Map<String, ScheduledOrderCompletion>) field.get(orderAdminService);
        scheduledCompletions.put(orderNumber, new ScheduledOrderCompletion(orderNumber, createdBy, pastTime));

        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(order));

        // when
        orderAdminService.processScheduledCompletions();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(scheduledCompletions).doesNotContainKey(orderNumber);
        verify(orderRepository, times(1)).findByOrderNumber(orderNumber);
        verify(statusLogRepository, times(1)).save(any(OrderStatusLog.class));
    }

    @Test
    @DisplayName("자동 완료 대상이 SHIPPING 상태가 아니면 완료 처리를 건너뛴다")
    void processScheduledCompletions_skipIfNotShipping() throws Exception {
        // given
        String orderNumber = "202507-abcdef-123456";
        Long createdBy = 99L;
        LocalDateTime pastTime = LocalDateTime.now().minusSeconds(1);
        
        Order order = new Order(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingInfo(new ShippingInfo("받는 사람", "010-1234-5678", "주소", LocalDate.now(), 3_000));

        Field field = OrderAdminServiceImpl.class.getDeclaredField("scheduledCompletions");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, ScheduledOrderCompletion> scheduledCompletions = 
            (Map<String, ScheduledOrderCompletion>) field.get(orderAdminService);
        scheduledCompletions.put(orderNumber, new ScheduledOrderCompletion(orderNumber, createdBy, pastTime));

        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(order));

        // when
        orderAdminService.processScheduledCompletions();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(scheduledCompletions).doesNotContainKey(orderNumber);
        verify(orderRepository, times(1)).findByOrderNumber(orderNumber);
        verify(statusLogRepository, never()).save(any(OrderStatusLog.class));
    }

    @Test
    @DisplayName("자동 완료 처리 중 주문을 찾을 수 없으면 스케줄에서 제거된다")
    void processScheduledCompletions_removeIfOrderNotFound() throws Exception {
        // given
        String orderNumber = "unknown-order";
        Long createdBy = 99L;
        LocalDateTime pastTime = LocalDateTime.now().minusSeconds(1);

        Field field = OrderAdminServiceImpl.class.getDeclaredField("scheduledCompletions");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, ScheduledOrderCompletion> scheduledCompletions = 
            (Map<String, ScheduledOrderCompletion>) field.get(orderAdminService);
        scheduledCompletions.put(orderNumber, new ScheduledOrderCompletion(orderNumber, createdBy, pastTime));

        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.empty());

        // when
        orderAdminService.processScheduledCompletions();

        // then
        assertThat(scheduledCompletions).doesNotContainKey(orderNumber);
        verify(orderRepository, times(1)).findByOrderNumber(orderNumber);
        verify(statusLogRepository, never()).save(any(OrderStatusLog.class));
    }
}