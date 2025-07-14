package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.dto.response.UserOrderAmountResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderInternalServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderInternalServiceImpl orderInternalService;

    @Test
    @DisplayName("최근 3개월간 사용자별 주문 금액 조회에 성공한다")
    void findOrderAmountGroupByUserLastThreeMonths_success() {
        // given
        List<UserOrderAmountResponse> expectedResponses = List.of(
                new UserOrderAmountResponse(1L, 120000L),
                new UserOrderAmountResponse(2L, 85000L),
                new UserOrderAmountResponse(3L, 45000L)
        );

        given(orderRepository.findOrderAmountGroupByUserLastThreeMonths())
                .willReturn(expectedResponses);

        // when
        List<UserOrderAmountResponse> result = orderInternalService.findOrderAmountGroupByUserLastThreeMonths();

        // then
        assertThat(result).isEqualTo(expectedResponses);
        assertThat(result).hasSize(3);
        assertThat(result.get(0).userNo()).isEqualTo(1L);
        assertThat(result.get(0).pureOrderAmount()).isEqualTo(120000L);
        assertThat(result.get(1).userNo()).isEqualTo(2L);
        assertThat(result.get(1).pureOrderAmount()).isEqualTo(85000L);
        assertThat(result.get(2).userNo()).isEqualTo(3L);
        assertThat(result.get(2).pureOrderAmount()).isEqualTo(45000L);

        verify(orderRepository).findOrderAmountGroupByUserLastThreeMonths();
    }

    @Test
    @DisplayName("최근 3개월간 주문이 없을 때 빈 리스트를 반환한다")
    void findOrderAmountGroupByUserLastThreeMonths_emptyResult() {
        // given
        List<UserOrderAmountResponse> emptyResponse = List.of();

        given(orderRepository.findOrderAmountGroupByUserLastThreeMonths())
                .willReturn(emptyResponse);

        // when
        List<UserOrderAmountResponse> result = orderInternalService.findOrderAmountGroupByUserLastThreeMonths();

        // then
        assertThat(result).isEqualTo(emptyResponse);
        assertThat(result).isEmpty();

        verify(orderRepository).findOrderAmountGroupByUserLastThreeMonths();
    }

    @Test
    @DisplayName("단일 사용자의 주문 금액 조회에 성공한다")
    void findOrderAmountGroupByUserLastThreeMonths_singleUser() {
        // given
        List<UserOrderAmountResponse> singleUserResponse = List.of(
                new UserOrderAmountResponse(1L, 200000L)
        );

        given(orderRepository.findOrderAmountGroupByUserLastThreeMonths())
                .willReturn(singleUserResponse);

        // when
        List<UserOrderAmountResponse> result = orderInternalService.findOrderAmountGroupByUserLastThreeMonths();

        // then
        assertThat(result).isEqualTo(singleUserResponse);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).userNo()).isEqualTo(1L);
        assertThat(result.get(0).pureOrderAmount()).isEqualTo(200000L);

        verify(orderRepository).findOrderAmountGroupByUserLastThreeMonths();
    }

    @Test
    @DisplayName("주문번호로 주문ID 조회에 성공한다")
    void findIdByOrderNumber_success() {
        // given
        String orderNumber = "202507-abcdef-123456";
        Long orderId = 1L;

        given(orderRepository.findIdByOrderNumber(anyString())).willReturn(orderId);

        // when
        Long result = orderInternalService.findIdByOrderNumber(orderNumber);

        // then
        assertThat(result).isEqualTo(orderId);
        verify(orderRepository, times(1)).findIdByOrderNumber(anyString());
    }

    @Test
    @DisplayName("주문번호가 존재하지 않을 때 null을 반환한다")
    void findIdByOrderNumber_orderNotFound_null() {
        // given
        String orderNumber = "not-found";

        given(orderRepository.findIdByOrderNumber(anyString())).willReturn(null);

        // when
        Long result = orderInternalService.findIdByOrderNumber(orderNumber);

        // then
        assertThat(result).isNull();
        verify(orderRepository, times(1)).findIdByOrderNumber(anyString());
    }

    @Test
    @DisplayName("주문ID로 주문번호 조회에 성공한다")
    void findOrderNumberById_success() {
        // given
        Long orderId = 1L;
        String orderNumber = "202507-abcdef-123456";

        given(orderRepository.findOrderNumberById(anyLong())).willReturn(orderNumber);

        // when
        String result = orderInternalService.findOrderNumberById(orderId);

        // then
        assertThat(result).isEqualTo(orderNumber);
        verify(orderRepository, times(1)).findOrderNumberById(anyLong());
    }

    @Test
    @DisplayName("주문ID로 주문을 찾지 못하면 null을 반환한다")
    void findOrderNumberById_orderNotFound_null() {
        // given
        Long orderId = 1L;

        given(orderRepository.findOrderNumberById(anyLong())).willReturn(null);

        // when
        String result = orderInternalService.findOrderNumberById(orderId);

        // then
        assertThat(result).isNull();
        verify(orderRepository, times(1)).findOrderNumberById(anyLong());
    }
}