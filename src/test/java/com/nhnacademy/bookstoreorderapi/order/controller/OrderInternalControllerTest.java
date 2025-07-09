package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.dto.response.UserOrderAmountResponse;
import com.nhnacademy.bookstoreorderapi.order.service.OrderInternalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderInternalControllerTest {

    @Mock
    private OrderInternalService orderInternalService;

    @InjectMocks
    private OrderInternalController orderInternalController;

    @Test
    @DisplayName("최근 3개월간 사용자별 주문 금액 조회에 성공한다")
    void getOrderAmountGroupByUserLastThreeMonth_success() {
        // given
        List<UserOrderAmountResponse> expectedResponses = List.of(
                new UserOrderAmountResponse(1L, 50000L),
                new UserOrderAmountResponse(2L, 75000L),
                new UserOrderAmountResponse(3L, 30000L)
        );
        
        given(orderInternalService.findOrderAmountGroupByUserLastThreeMonths())
                .willReturn(expectedResponses);

        // when
        ResponseEntity<List<UserOrderAmountResponse>> response = 
                orderInternalController.getOrderAmountGroupByUserLastThreeMonth();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponses);
        assertThat(response.getBody()).hasSize(3);
        
        verify(orderInternalService).findOrderAmountGroupByUserLastThreeMonths();
    }

    @Test
    @DisplayName("최근 3개월간 주문이 없을 때 빈 리스트를 반환한다")
    void getOrderAmountGroupByUserLastThreeMonth_emptyList() {
        // given
        List<UserOrderAmountResponse> emptyResponse = List.of();
        
        given(orderInternalService.findOrderAmountGroupByUserLastThreeMonths())
                .willReturn(emptyResponse);

        // when
        ResponseEntity<List<UserOrderAmountResponse>> response = 
                orderInternalController.getOrderAmountGroupByUserLastThreeMonth();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(emptyResponse);
        assertThat(response.getBody()).isEmpty();
        
        verify(orderInternalService).findOrderAmountGroupByUserLastThreeMonths();
    }
}