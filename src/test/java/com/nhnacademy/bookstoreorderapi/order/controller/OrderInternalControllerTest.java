package com.nhnacademy.bookstoreorderapi.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.bookstoreorderapi.order.dto.request.ValidatePurchaseRequest;
import com.nhnacademy.bookstoreorderapi.order.service.OrderInternalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(OrderInternalController.class)
@ActiveProfiles("test")
class OrderInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderInternalService orderInternalService;

    @Test
    @DisplayName("주문번호로 주문ID를 조회하는데 성공하면 200을 응답한다")
    void getIdByOrderNumber_success() throws Exception {
        // given
        String orderNumber = "202507-abcdef-123456";
        Long orderId = 1L;

        given(orderInternalService.findIdByOrderNumber(anyString())).willReturn(orderId);

        // when & then
        mockMvc.perform(get("/internal/orders/id")
                        .param("orderNumber", orderNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(orderInternalService, times(1)).findIdByOrderNumber(anyString());
    }

    @Test
    @DisplayName("주문ID로 주문번호를 조회하는데 성공하면 200을 응답한다")
    void getOrderNumberById_success() throws Exception {
        // given
        Long orderId = 1L;
        String orderNumber = "202507-abcdef-123456";

        given(orderInternalService.findOrderNumberById(anyLong())).willReturn(orderNumber);

        // when & then
        mockMvc.perform(get("/internal/orders/{orderId}/orderNumber", orderId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(orderInternalService, times(1)).findOrderNumberById(anyLong());
    }

    @Test
    @DisplayName("책 구매 검증에 성공하면 200과 true를 응답한다")
    void validatePurchase_purchased_success() throws Exception {
        // given
        ValidatePurchaseRequest request = new ValidatePurchaseRequest(1L, 1L);

        given(orderInternalService.validatePurchase(any(ValidatePurchaseRequest.class))).willReturn(true);

        // when & then
        mockMvc.perform(get("/internal/orders/exists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(orderInternalService, times(1)).validatePurchase(any(ValidatePurchaseRequest.class));
    }

    @Test
    @DisplayName("책을 구매하지 않은 경우 200과 false를 응답한다")
    void validatePurchase_notPurchased_success() throws Exception {
        // given
        ValidatePurchaseRequest request = new ValidatePurchaseRequest(1L, 999L);

        given(orderInternalService.validatePurchase(any(ValidatePurchaseRequest.class))).willReturn(false);

        // when & then
        mockMvc.perform(get("/internal/orders/exists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(orderInternalService, times(1)).validatePurchase(any(ValidatePurchaseRequest.class));
    }
}