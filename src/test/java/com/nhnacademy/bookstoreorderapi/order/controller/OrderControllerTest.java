package com.nhnacademy.bookstoreorderapi.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.PurchaseVerificationResponse;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                "[12345] 서울특별시 강남구 테헤란로 123 10층",
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

    @Test
    @DisplayName("회원 주문 전체 조회에 성공한다")
    void getAllOrdersByUserId_success() throws Exception {
        // given
        String xUserId = "testUser";
        List<OrderSummaryResponse> orderSummaries = List.of(
                new OrderSummaryResponse(LocalDate.now(), "ORDER-20240101-001", "홍길동", 10_000L),
                new OrderSummaryResponse(LocalDate.now().minusDays(1), "ORDER-20240101-002", "김철수", 10_000L)
        );
        Page<OrderSummaryResponse> pageResult = new PageImpl<>(orderSummaries, PageRequest.of(0, 10), 10);
        
        given(orderService.findAllByUserId(xUserId)).willReturn(pageResult);

        // when & then
        mockMvc.perform(get("/orders")
                        .header("X-USER-ID", xUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].orderId").value("ORDER-20240101-001"))
                .andExpect(jsonPath("$.content[0].receiverName").value("홍길동"))
                .andExpect(jsonPath("$.content[1].orderId").value("ORDER-20240101-002"))
                .andExpect(jsonPath("$.content[1].receiverName").value("김철수"));

        verify(orderService).findAllByUserId(xUserId);
    }

    @Test
    @DisplayName("회원 주문 전체 조회 시 빈 페이지를 반환한다")
    void getAllOrdersByUserId_emptyPage() throws Exception {
        // given
        String xUserId = "testUser";
        Page<OrderSummaryResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        
        given(orderService.findAllByUserId(xUserId)).willReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/orders")
                        .header("X-USER-ID", xUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));

        verify(orderService).findAllByUserId(xUserId);
    }

    @Test
    @DisplayName("X-USER-ID 헤더 없이 요청하면 400 에러가 발생한다")
    void getAllOrdersByUserId_missingXUserIdHeader_badRequest() throws Exception {
        // when & then
        mockMvc.perform(get("/orders"))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).findAllByUserId(anyString());
    }

    @Test
    @DisplayName("회원 주문 상세 조회에 성공한다")
    void getOrder_success() throws Exception {
        // given
        String xUserId = "testUser";
        String orderId = "ORDER-20240101-001";
        
        given(orderService.findByOrderId(orderId, xUserId)).willReturn(orderResponse);

        // when & then
        mockMvc.perform(get("/orders/{orderId}", orderId)
                        .header("X-USER-ID", xUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ORDER-20240101-001"))
                .andExpect(jsonPath("$.receiverName").value("홍길동"))
                .andExpect(jsonPath("$.receiverPhoneNumber").value("01012345678"))
                .andExpect(jsonPath("$.address").value("서울특별시 강남구 테헤란로 123"))
                .andExpect(jsonPath("$.totalAmount").value(55000))
                .andExpect(jsonPath("$.deliveryFee").value(3000))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService).findByOrderId(orderId, xUserId);
    }

    @Test
    @DisplayName("회원 주문 상세 조회 시 X-USER-ID 헤더가 없으면 400 에러가 발생한다")
    void getOrder_missingXUserIdHeader_badRequest() throws Exception {
        // given
        String orderId = "ORDER-20240101-001";

        // when & then
        mockMvc.perform(get("/orders/{orderId}", orderId))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).findByOrderId(anyString(), anyString());
    }

    // ========== 구매 확인 테스트 ==========

    @Test
    @DisplayName("구매 확인 성공 - 구매 이력이 있는 경우")
    void verifyPurchase_success_withValidPurchase() throws Exception {
        // given
        String xUserId = "testUser";
        Long bookId = 123L;
        PurchaseVerificationResponse expectedResponse = new PurchaseVerificationResponse(1L, bookId, true);
        
        given(orderService.verifyPurchase(xUserId, bookId)).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/orders/verify-purchase")
                        .header("X-USER-ID", xUserId)
                        .param("bookId", bookId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userNo").value(1L))
                .andExpect(jsonPath("$.bookId").value(123L))
                .andExpect(jsonPath("$.isValid").value(true));
        
        verify(orderService).verifyPurchase(xUserId, bookId);
    }

    @Test
    @DisplayName("구매 확인 성공 - 구매 이력이 없는 경우")
    void verifyPurchase_success_withNoPurchase() throws Exception {
        // given
        String xUserId = "testUser";
        Long bookId = 456L;
        PurchaseVerificationResponse expectedResponse = new PurchaseVerificationResponse(1L, bookId, false);
        
        given(orderService.verifyPurchase(xUserId, bookId)).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/orders/verify-purchase")
                        .header("X-USER-ID", xUserId)
                        .param("bookId", bookId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userNo").value(1L))
                .andExpect(jsonPath("$.bookId").value(456L))
                .andExpect(jsonPath("$.isValid").value(false));
        
        verify(orderService).verifyPurchase(xUserId, bookId);
    }

    @Test
    @DisplayName("구매 확인 - X-USER-ID 헤더 없이 요청하면 400 에러 발생")
    void verifyPurchase_missingXUserIdHeader_badRequest() throws Exception {
        // given
        Long bookId = 123L;

        // when & then
        mockMvc.perform(get("/orders/verify-purchase")
                        .param("bookId", bookId.toString()))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).verifyPurchase(anyString(), any(Long.class));
    }

    @Test
    @DisplayName("구매 확인 - bookId 파라미터 없이 요청하면 400 에러 발생")
    void verifyPurchase_missingBookIdParam_badRequest() throws Exception {
        // given
        String xUserId = "testUser";

        // when & then
        mockMvc.perform(get("/orders/verify-purchase")
                        .header("X-USER-ID", xUserId))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).verifyPurchase(anyString(), any(Long.class));
    }

    @Test
    @DisplayName("구매 확인 - 잘못된 bookId 형식으로 요청하면 400 에러 발생")
    void verifyPurchase_invalidBookIdFormat_badRequest() throws Exception {
        // given
        String xUserId = "testUser";
        String invalidBookId = "invalid";

        // when & then
        mockMvc.perform(get("/orders/verify-purchase")
                        .header("X-USER-ID", xUserId)
                        .param("bookId", invalidBookId))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).verifyPurchase(anyString(), any(Long.class));
    }
}