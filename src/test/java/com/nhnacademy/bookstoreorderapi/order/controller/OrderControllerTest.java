package com.nhnacademy.bookstoreorderapi.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.bookstoreorderapi.order.dto.request.CreateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderStatusRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.UpdateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.CreateOrderResponse;
import com.nhnacademy.bookstoreorderapi.order.exception.badrequest.InvalidRequestException;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.UserNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("회원 주문 생성에 성공하면 201을 응답한다")
    void createOrder_member_success() throws Exception {
        // given
        String xUserId = "testUser";

        CreateOrderRequest.CreateOrderItemRequest itemRequest = new CreateOrderRequest.CreateOrderItemRequest(1L, 1);
        CreateOrderRequest request = new CreateOrderRequest(List.of(itemRequest));

        CreateOrderResponse.CreateOrderItemResponse itemResponse = new CreateOrderResponse.CreateOrderItemResponse(1L, "책제목1", 100, 1, true);
        CreateOrderResponse response = new CreateOrderResponse("202507-abcdef-123456", List.of(itemResponse));

        given(orderService.createOrder(any(), anyString())).willReturn(response);

        // when & then
        mockMvc.perform(post("/orders")
                        .header("X-USER-ID", xUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("202507-abcdef-123456"))
                .andExpect(jsonPath("$.orderItems").isArray());
    }

    @Test
    @DisplayName("비회원 주문 생성에 성공하면 201을 응답한다")
    void createOrder_guest_success() throws Exception {
        // given
        CreateOrderRequest.CreateOrderItemRequest itemRequest = new CreateOrderRequest.CreateOrderItemRequest(1L, 1);
        CreateOrderRequest request = new CreateOrderRequest(List.of(itemRequest));

        CreateOrderResponse.CreateOrderItemResponse itemResponse = new CreateOrderResponse.CreateOrderItemResponse(1L, "책제목1", 100, 1, true);
        CreateOrderResponse response = new CreateOrderResponse("202507-abcdef-123456", List.of(itemResponse));

        given(orderService.createOrder(any(), isNull())).willReturn(response);

        // when & then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("202507-abcdef-123456"))
                .andExpect(jsonPath("$.orderItems").isArray());
    }

    @Test
    @DisplayName("주문 생성 요청 데이터가 잘못되면 400을 응답한다")
    void createOrder_beanValidation_fail() throws Exception {
        // given (bookId가 양수가 아닐 때)
        CreateOrderRequest.CreateOrderItemRequest itemRequest = new CreateOrderRequest.CreateOrderItemRequest(0L, 1);
        CreateOrderRequest request = new CreateOrderRequest(List.of(itemRequest));

        // when & then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(CreateOrderRequest.class), anyString());
    }

    @Test
    @DisplayName("주문 생성 요청 데이터가 null 이면 주문 생성에 실패한다")
    void createOrder_paramIsNull_fail() throws Exception {
         // when & then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(CreateOrderRequest.class), anyString());
    }

    @Test
    @DisplayName("xUserId와 일치하지 않는 userNo가 없을 때 404를 응답한다")
    void createOrder_userNotFound_fail() throws Exception {
        // given
        String xUserId = "user-not-found";
        CreateOrderRequest request = new CreateOrderRequest(List.of(new CreateOrderRequest.CreateOrderItemRequest(1L, 1)));
        given(orderService.createOrder(any(CreateOrderRequest.class), anyString())).willThrow(new UserNotFoundException("유저를 찾을 수 없습니다 - userId: " + xUserId));

        // when
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", xUserId)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class), anyString());
    }

    @Test
    @DisplayName("완료되지 않은 주문 조회에 성공한다")
    void getUnfinishedOrder_success() throws Exception {
        // given
        String orderNumber = "202507-abcdef-123456";

        // when & then
        mockMvc.perform(get("/orders/{orderNumber}/input-detail", orderNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("완료되지 않은 주문 조회에 실패하면 400을 응답한다(이유: orderNumber is blank)")
    void getUnfinishedOrder_invalidRequest_fail() throws Exception {
        mockMvc.perform(get("/orders/{orderNumber}/input-detail", " ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertThatThrownBy(() -> {
            throw new InvalidRequestException("주문번호가 비어있습니다");
        })
        .isInstanceOf(InvalidRequestException.class);

        verify(orderService, never()).getUnfinishedOrder(anyString(), anyString());
    }

    @Test
    @DisplayName("완료되지 않은 주문 조회에 실패하면 404를 응답한다(이유: Order Not Found)")
    void getUnfinishedOrder_notFound_fail() throws Exception {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = "testUser";
        given(orderService.getUnfinishedOrder(anyString(), anyString())).willThrow(new OrderNotFoundException("주문을 찾을 수 없습니다: orderNumber=" + orderNumber));

        mockMvc.perform(get("/orders/{orderNumber}/input-detail", orderNumber)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", xUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("회원 주문 전체 조회에 성공하면 200을 응답한다")
    void getAllOrdersByUserId_success() throws Exception {
        // given
        String xUserId = "testUser";

        // when & then
        mockMvc.perform(get("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", xUserId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주문 업데이트(포장 및 배송정보 업데이트)에 성공한다")
    void updateOrder_success() throws Exception {
        // given
        String orderNumber = "202507-abcdef-123456";
        UpdateOrderRequest updateOrderRequest = new UpdateOrderRequest(
                List.of(new UpdateOrderRequest.WrappingRequest(1L, 1L)),
                "받는 사람",
                "010-1234-5678",
                "우주",
                LocalDate.now().plusDays(3)
        );
        String xUserId = "testMember";

        // when & then
        mockMvc.perform(put("/orders/{orderNumber}", orderNumber)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateOrderRequest))
                        .header("X-USER-ID", xUserId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주문 업데이트(포장 및 배송정보 업데이트)에 실패한다(이유: orderNumber is blank)")
    void updateOrder_orderNumberIsBlank_fail() throws Exception {
        // given
        String orderNumber = " ";
        UpdateOrderRequest updateOrderRequest = new UpdateOrderRequest(
                List.of(new UpdateOrderRequest.WrappingRequest(1L, 1L)),
                "받는 사람",
                "010-1234-5678",
                "우주",
                LocalDate.now().plusDays(3)
        );
        String xUserId = "testMember";

        // when & then
        mockMvc.perform(put("/orders/{orderNumber}", orderNumber)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateOrderRequest))
                        .header("X-USER-ID", xUserId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 상세 조회에 성공하면 200을 응답한다")
    void getOrder_success() throws Exception {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = "testUser";

        // when & then
        mockMvc.perform(get("/orders/{orderNumber}", orderNumber)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", xUserId))
                .andExpect(status().isOk());

        verify(orderService, times(1)).findByOrderNumber(anyString(), anyString());
    }

    @Test
    @DisplayName("주문 상세 조회에 실패하면 400을 응답한다(이유: orderNumber is blank)")
    void getOrder_invalidRequest_fail() throws Exception {
        // when & then
        mockMvc.perform(get("/orders/{orderNumber}", " ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).findByOrderNumber(anyString(), anyString());
    }

    @Test
    @DisplayName("주문 상세 조회에 실패하면 404를 응답한다(이유: Order Not Found)")
    void getOrder_notFound_fail() throws Exception {
        // given
        String orderNumber = "202507-abcdef-123456";
        String xUserId = "testUser";
        given(orderService.findByOrderNumber(anyString(), anyString())).willThrow(new OrderNotFoundException("주문을 찾을 수 없습니다: orderNumber=" + orderNumber));

        // when & then
        mockMvc.perform(get("/orders/{orderNumber}", orderNumber)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", xUserId))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).findByOrderNumber(anyString(), anyString());
    }

    @Test
    @DisplayName("반품 요청에 성공하면 200을 응답한다")
    void changeOrderStatus_success() throws Exception {
        // given
        String orderNumber = "202507-abcdef-123456";
        OrderStatusRequest request = new OrderStatusRequest(OrderStatusRequest.OrderAction.RETURN, "파손", true);
        String xUserId = "testUser";

        // when & then
        mockMvc.perform(put("/orders/{orderNumber}/status", orderNumber)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", xUserId)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(orderService, times(1)).changeOrderStatus(anyString(), any(), anyString());
    }
}
