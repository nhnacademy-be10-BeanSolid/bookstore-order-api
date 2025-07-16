package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.common.resolver.XUserIdResolver;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.exception.forbidden.NotAdminException;
import com.nhnacademy.bookstoreorderapi.order.service.OrderAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(OrderAdminController.class)
@ActiveProfiles("test")
class OrderAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderAdminService orderAdminService;
    @MockBean
    private XUserIdResolver xUserIdResolver;

    @Test
    @DisplayName("전체 주문 조회에 성공한다.(페이징 처리됨)")
    void getAllOrders_success() throws Exception {
        // given
        String xUserId = "admin";
        Pageable pageable = PageRequest.of(0, 1);
        List<OrderSummaryResponse> responses = List.of(
                new OrderSummaryResponse(LocalDate.now(), "202507-abcdef-123456", "받는 사람", 100L, "PENDING"),
                new OrderSummaryResponse(LocalDate.now(), "202508-abcdef-123456", "받는 사람", 100L, "SHIPPING"));
        Page<OrderSummaryResponse> responsePage = new PageImpl<>(responses, pageable, responses.size());

        given(orderAdminService.getAllOrders(any(), anyString())).willReturn(responsePage);
        given(xUserIdResolver.isAdmin(anyString())).willReturn(true);

        // when & then
        mockMvc.perform(get("/admin/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", xUserId))
                .andExpect(status().isOk());

        verify(orderAdminService, times(1)).getAllOrders(any(), anyString());
    }

    @ParameterizedTest(name = "X-USER-ID 헤더가 빈 값이거나 공백이면 전체 주문 조회에 실패한다")
    @ValueSource(strings = {"", " "})
    void getAllOrders_blankXUserIdHeader_fail(String xUserId) throws Exception {
        // given
        given(orderAdminService.getAllOrders(any(), anyString()))
                .willThrow(NotAdminException.class);
        // when & then
        mockMvc.perform(get("/admin/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", xUserId))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("관리자가 주문상태 변경(대기 -> 배송) 요청을 하면 200 응답을 반환한다")
    void changeStatusToShipping_success() throws Exception {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = "admin";

        given(xUserIdResolver.isAdmin(anyString())).willReturn(true);

        // when & then
        mockMvc.perform(put("/admin/orders/{orderNumber}/status", orderNumber)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", xUserId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("관리자가 아닌데 주문상태 변경(대기 -> 배송) 요청을 하면 403 응답을 반환한다")
    void changeStatusToShipping_notAdmin_fail() throws Exception {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = "notAdmin";

        given(orderAdminService.changeStatusToShipping(anyString(), anyString()))
                .willThrow(NotAdminException.class);

        // when & then
        mockMvc.perform(put("/admin/orders/{orderNumber}/status", orderNumber)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", xUserId))
                .andExpect(status().isForbidden());
    }

}