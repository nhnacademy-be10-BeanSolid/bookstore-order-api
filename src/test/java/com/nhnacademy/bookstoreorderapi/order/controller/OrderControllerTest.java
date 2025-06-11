package com.nhnacademy.bookstoreorderapi.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderResponseDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderItemDto;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /orders - 성공적으로 주문 목록 반환")
    void listAllOrders() throws Exception {
        OrderResponseDto sample = OrderResponseDto.builder()
                .orderId(1L)
                .totalPrice(20000)
                .deliveryFee(3000)
                .finalPrice(23000)
                .message("[회원 ID: 123] 주문 생성됨 / 총액: 20000원 / 배송비: 3000원 / 결제금액: 23000원")
                .build();

        given(orderService.listAll()).willReturn(List.of(sample));

        mockMvc.perform(get("/orders")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(sample))));
    }

    @Test
    @DisplayName("POST /orders - 회원 주문 생성 성공")
    void createMemberOrderSuccess() throws Exception {
        OrderRequestDto req = OrderRequestDto.builder()
                .orderType("member")
                .userId(123L)
                .deliveryDate(LocalDate.of(2025, 6, 12))
                .items(List.of(new OrderItemDto(3L, 1, false, null)))
                .build();

        OrderResponseDto resp = OrderResponseDto.builder()
                .orderId(1L)
                .totalPrice(10000)
                .deliveryFee(3000)
                .finalPrice(13000)
                .message("[회원 ID: 123] 주문 생성됨 / 총액: 10000원 / 배송비: 3000원 / 결제금액: 13000원")
                .build();

        given(orderService.createOrder(any(OrderRequestDto.class))).willReturn(resp);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(resp)));
    }

    @Test
    @DisplayName("POST /orders - 비회원 주문 생성 성공")
    void createGuestOrderSuccess() throws Exception {
        OrderRequestDto req = OrderRequestDto.builder()
                .orderType("guest")
                .guestName("홍길동")
                .guestPhone("010-1234-5678")
                .deliveryDate(LocalDate.of(2025, 6, 15))
                .items(List.of(new OrderItemDto(1L, 2, true, null)))
                .build();

        OrderResponseDto resp = OrderResponseDto.builder()
                .orderId(2L)
                .totalPrice(24000)
                .deliveryFee(3000)
                .finalPrice(27000)
                .message("[비회원: 홍길동 (010-1234-5678)] 주문 생성됨 / 총액: 24000원 / 배송비: 3000원 / 결제금액: 27000원")
                .build();

        given(orderService.createOrder(any(OrderRequestDto.class))).willReturn(resp);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(resp)));
    }

    @Test
    @DisplayName("POST /orders - 잘못된 orderType 으로 인한 400 응답")
    void createOrderInvalidType() throws Exception {
        OrderRequestDto req = OrderRequestDto.builder()
                .orderType("invalid")
                .deliveryDate(LocalDate.of(2025, 6, 15))
                .items(List.of(new OrderItemDto(1L, 1, false, null)))
                .build();

        // validateOrderType 에서 IllegalArgumentException 발생
        doThrow(new IllegalArgumentException("orderType은 'member' 또는 'guest'여야 합니다."))
                .when(orderService).createOrder(any(OrderRequestDto.class));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}