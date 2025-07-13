package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderAdminServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
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
        Page<OrderSummaryResponse> result = orderAdminService.getAllOrders(xUserId, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).isEqualTo(responses);

        verify(orderRepository, times(1)).findAllOrderSummary(any());
    }
}