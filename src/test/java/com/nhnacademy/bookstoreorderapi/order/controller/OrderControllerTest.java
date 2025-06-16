package com.nhnacademy.bookstoreorderapi.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderResponseDto sampleGuestOrder;
    private OrderResponseDto sampleMemberOrder;
    private StatusChangeResponseDto sampleStatusChange;
    private OrderStatusLogDto sampleLog;

    @BeforeEach
    void setUp() {
        sampleGuestOrder = OrderResponseDto.builder()
                .orderId(1L)
                .totalPrice(10000)
                .deliveryFee(5000)
                .finalPrice(15000)
                .message("[비회원: 테스트 (010-0000-0001)] 주문 생성됨 / 총액: 10000원 / 배송비: 5000원 / 결제금액: 15000원")
                .build();

        sampleMemberOrder = OrderResponseDto.builder()
                .orderId(2L)
                .totalPrice(30000)
                .deliveryFee(0)
                .finalPrice(30000)
                .message("[회원 ID: 42] 주문 생성됨 / 총액: 30000원 / 배송비: 0원 / 결제금액: 30000원")
                .build();

        sampleStatusChange = StatusChangeResponseDto.builder()
                .orderId(3L)
                .oldStatus(OrderStatus.PENDING)
                .newStatus(OrderStatus.SHIPPING)
                .changedBy(999L)
                .memo("발송 준비 완료")
                .changedAt(LocalDateTime.now())
                .build();

        sampleLog = OrderStatusLogDto.builder()
                .orderStateId(1L)
                .orderId(3L)
                .oldStatus(OrderStatus.PENDING)
                .newStatus(OrderStatus.SHIPPING)
                .changedBy(999L)
                .memo("발송 준비 완료")
                .changedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void listMyOrders_returnsOkAndJsonArray() throws Exception {
        given(orderService.listByUser("42"))
                .willReturn(Arrays.asList(sampleGuestOrder, sampleMemberOrder));

        mockMvc.perform(get("/orders")
                        .param("userId", "42")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(1L))
                .andExpect(jsonPath("$[1].orderId").value(2L));
    }

    @Test
    void createOrder_guestValid_returnsOk() throws Exception {
        OrderRequestDto req = OrderRequestDto.builder()
                .orderType("guest")
                .guestId(1L)
                .items(Collections.singletonList(
                        OrderItemDto.builder()
                                .bookId(1L)
                                .quantity(1)
                                .giftWrapped(false)
                                .build()
                ))
                .build();

        given(orderService.createOrder(any(OrderRequestDto.class)))
                .willReturn(sampleGuestOrder);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1L));
    }

    @Test
    void createOrder_memberValid_returnsOk() throws Exception {
        OrderRequestDto req = OrderRequestDto.builder()
                .orderType("member")
                .userId("42")
                .orderAddress("서울특별시 강남구 테헤란로 123")   // ⭐️ 추가
                .deliveryDate(LocalDate.of(2025, 6, 20))
                .items(Collections.singletonList(
                        OrderItemDto.builder()
                                .bookId(2L)
                                .quantity(3)
                                .giftWrapped(true)
                                .wrappingId(1L)
                                .build()
                ))
                .build();

        given(orderService.createOrder(any(OrderRequestDto.class)))
                .willReturn(sampleMemberOrder);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(2L));
    }

    @Test
    void changeStatus_valid_returnsOk() throws Exception {
        StatusChangeRequestDto dto = new StatusChangeRequestDto();
        dto.setNewStatus(OrderStatus.SHIPPING);
        dto.setChangedBy(999L);
        dto.setMemo("발송 준비 완료");

        given(orderService.changeStatus(eq(3L), any(), anyLong(), anyString()))
                .willReturn(sampleStatusChange);

        mockMvc.perform(patch("/orders/3/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newStatus").value("SHIPPING"));
    }

    @Test
    void cancelOrder_valid_returnsOk() throws Exception {
        CancelOrderRequestDto dto = new CancelOrderRequestDto();
        dto.setReason("고객 변심");

        mockMvc.perform(post("/orders/2/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("주문이 정상적으로 취소되었습니다."));
    }

    @Test
    void requestReturn_valid_returnsOk() throws Exception {

        ReturnRequestDto dto = ReturnRequestDto.builder().reason("테스트 이유").requestedAt(LocalDateTime.now()).damaged(false).build();
        given(orderService.requestReturn(3L, dto)).willReturn(25000);

        mockMvc.perform(post("/orders/3/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void getStatusLog_valid_returnsOk() throws Exception {
        given(orderService.getStatusLog(3L))
                .willReturn(Collections.singletonList(sampleLog));

        mockMvc.perform(get("/orders/3/status-log")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderStateId").value(1L))
                .andExpect(jsonPath("$[0].orderId").value(3L));
    }
}