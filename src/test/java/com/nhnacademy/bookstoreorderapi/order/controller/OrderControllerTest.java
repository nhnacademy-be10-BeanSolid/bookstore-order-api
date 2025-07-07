package com.nhnacademy.bookstoreorderapi.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private OrderRequest validOrderRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        List<OrderRequest.OrderItemRequest> items = List.of(
                new OrderRequest.OrderItemRequest(1L, 2, 15000L, 1L),
                new OrderRequest.OrderItemRequest(2L, 1, 25000L, 2L)
        );

        validOrderRequest = new OrderRequest(
                "홍길동",
                "01012345678",
                "12345",
                "서울특별시 강남구 테헤란로 123",
                "10층",
                LocalDate.now().plusDays(3),
                items
        );

        orderResponse = new OrderResponse(
                1L, // id
                "ORDER-20240101-001", // orderId
                "PENDING", // status
                LocalDate.now(), // orderDate
                "홍길동", // receiverName
                "01012345678", // receiverPhoneNumber
                "서울특별시 강남구 테헤란로 123", // address
                LocalDate.now().plusDays(3), // requestedDeliveryDate
                3000, // deliveryFee
                55000L // totalAmount
        );
    }

    @Test
    @DisplayName("회원 주문 생성에 성공한다")
    void createOrder_withValidMemberRequest_success() throws Exception {
        // given
        String xUserId = "testUser";
        given(orderService.createOrder(any(OrderRequest.class), eq(xUserId)))
                .willReturn(orderResponse);

        // when & then
        mockMvc.perform(post("/orders")
                        .header("X-USER-ID", xUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ORDER-20240101-001"))
                .andExpect(jsonPath("$.receiverName").value("홍길동"))
                .andExpect(jsonPath("$.receiverPhoneNumber").value("01012345678"))
                .andExpect(jsonPath("$.address").value("서울특별시 강남구 테헤란로 123"))
                .andExpect(jsonPath("$.totalAmount").value(55000))
                .andExpect(jsonPath("$.deliveryFee").value(3000))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService).createOrder(any(OrderRequest.class), eq(xUserId));
    }

    @Test
    @DisplayName("비회원 주문 생성에 성공한다")
    void createOrder_withValidGuestRequest_success() throws Exception {
        // given
        String xUserId = "";
        given(orderService.createOrder(any(OrderRequest.class), eq(xUserId)))
                .willReturn(orderResponse);

        // when & then
        mockMvc.perform(post("/orders")
                        .header("X-USER-ID", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ORDER-20240101-001"))
                .andExpect(jsonPath("$.receiverName").value("홍길동"));

        verify(orderService).createOrder(any(OrderRequest.class), eq(xUserId));
    }

    @Test
    @DisplayName("잘못된 Content-Type으로 요청 시 415 에러가 발생한다")
    void createOrder_withWrongContentType_unsupportedMediaType() throws Exception {
        // given
        String xUserId = "testUser";

        // when & then
        mockMvc.perform(post("/orders")
                        .header("X-USER-ID", xUserId)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());

        verify(orderService, never()).createOrder(any(OrderRequest.class), anyString());
    }

    @Test
    @DisplayName("잘못된 JSON 형식으로 요청 시 400 에러가 발생한다")
    void createOrder_withInvalidJson_badRequest() throws Exception {
        // given
        String xUserId = "testUser";
        String invalidJson = "{invalid json}";

        // when & then
        mockMvc.perform(post("/orders")
                        .header("X-USER-ID", xUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(OrderRequest.class), anyString());
    }
}