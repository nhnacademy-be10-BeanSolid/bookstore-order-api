package com.nhnacademy.bookstoreorderapi.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.ResourceNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("GET /orders - 성공적으로 주문 목록 반환")
    void listAllOrders() throws Exception {
        OrderResponseDto sample = OrderResponseDto.builder()
                .orderId(1L)
                .totalPrice(20000)
                .deliveryFee(Order.DELIVERY_FEE)
                .finalPrice(23000)
                .message("[회원 ID: 123] 주문 생성됨 / 총액: 20000원 / 배송비: 5000원 / 결제금액: 23000원")
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
                .userId("123L")
                .deliveryDate(LocalDate.of(2025, 12, 12))
                .items(List.of(new OrderItemDto(3L, 1, false, null)))
                .build();

        OrderResponseDto resp = OrderResponseDto.builder()
                .orderId(1L)
                .totalPrice(10000)
                .deliveryFee(Order.DELIVERY_FEE)
                .finalPrice(15000)
                .message("[회원 ID: 123] 주문 생성됨 / 총액: 10000원 / 배송비: 5000원 / 결제금액: 15000원")
                .orderStatus(OrderStatus.PENDING)
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
                .deliveryDate(LocalDate.of(2025, 12, 15))
                .items(List.of(new OrderItemDto(1L, 2, true, null)))
                .build();

        OrderResponseDto resp = OrderResponseDto.builder()
                .orderId(2L)
                .totalPrice(24000)
                .deliveryFee(Order.DELIVERY_FEE)
                .finalPrice(29000)
                .message("[비회원: 홍길동 (010-1234-5678)] 주문 생성됨 / 총액: 24000원 / 배송비: 5000원 / 결제금액: 29000원")
                .orderStatus(OrderStatus.PENDING)
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

        doThrow(new IllegalArgumentException("orderType은 'member' 또는 'guest'여야 합니다."))
                .when(orderService).createOrder(any(OrderRequestDto.class));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"error\":\"orderType은 'member' 또는 'guest'여야 합니다.\"}"));
    }

    @Test
    @DisplayName("PATCH /orders/{id}/status - 상태 변경 성공")
    void changeStatusSuccess() throws Exception {
        StatusChangeResponseDto resp = StatusChangeResponseDto.builder()
                .orderId(1L)
                .oldStatus(OrderStatus.PENDING)
                .newStatus(OrderStatus.SHIPPING)
                .changedAt(LocalDateTime.now())
                .changedBy(42L)
                .memo("발송 준비 완료")
                .build();

        given(orderService.changeStatus(eq(1L), eq(OrderStatus.SHIPPING), eq(42L), eq("발송 준비 완료")))
                .willReturn(resp);

        StatusChangeDto req = new StatusChangeDto(
                OrderStatus.SHIPPING, 42L, "발송 준비 완료"
        );

        mockMvc.perform(patch("/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(resp)));
    }

    @Test
    @DisplayName("PATCH /orders/{id}/status - 잘못된 전이로 인한 400 응답")
    void changeStatusFail() throws Exception {
        StatusChangeDto req = new StatusChangeDto(
                OrderStatus.COMPLETED, 42L, "재설정 시도"
        );

        doThrow(new IllegalStateException("상태 전이 불가"))
                .when(orderService).changeStatus(eq(1L), any(), any(), any());

        mockMvc.perform(patch("/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"error\":\"상태 전이 불가\"}"));
    }

    @Test
    @DisplayName("POST /orders/{id}/cancel - 주문 취소 성공")
    void cancelOrderSuccess() throws Exception {
        doNothing().when(orderService).cancelOrder(1L, "고객변심");

        mockMvc.perform(post("/orders/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"고객변심\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"주문이 정상적으로 취소되었습니다.\"}"));
    }

    @Test
    @DisplayName("POST /orders/{id}/cancel - 주문 취소 실패")
    void cancelOrderFail() throws Exception {
        doThrow(new ResourceNotFoundException("주문 없음"))
                .when(orderService).cancelOrder(eq(1L), any());

        mockMvc.perform(post("/orders/1/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"error\":\"주문 없음\"}"));
    }

    @Test
    @DisplayName("GET /orders/{id}/status-log - 상태 로그 조회 성공")
    void getStatusLogSuccess() throws Exception {
        OrderStatusLogDto log = OrderStatusLogDto.builder()
                .orderStateId(1L)
                .orderId(1L)
                .oldStatus(OrderStatus.PENDING)
                .newStatus(OrderStatus.SHIPPING)
                .changedAt(LocalDateTime.now())
                .changedBy(42L)
                .memo("발송")
                .build();

        given(orderService.getStatusLog(1L)).willReturn(List.of(log));

        mockMvc.perform(get("/orders/1/status-log")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(log))));
    }

    @Test
    @DisplayName("GET /orders/{id}/status-log - 주문 없음으로 인한 400 응답")
    void getStatusLogNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("주문 없음"))
                .when(orderService).getStatusLog(1L);

        mockMvc.perform(get("/orders/1/status-log"))
                .andExpect(status().isBadRequest());
    }
}