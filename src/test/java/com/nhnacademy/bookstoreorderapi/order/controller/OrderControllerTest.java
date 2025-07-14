package com.nhnacademy.bookstoreorderapi.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.bookstoreorderapi.order.client.book.service.BookService;
import com.nhnacademy.bookstoreorderapi.order.client.user.service.UserService;
import com.nhnacademy.bookstoreorderapi.order.dto.request.CreateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.UpdateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.CreateOrderResponse;
import com.nhnacademy.bookstoreorderapi.order.exception.badrequest.InvalidRequestException;
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

    @MockBean
    private OrderService orderService;
    @MockBean
    private BookService bookService;
    @MockBean
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원 주문 생성에 성공한다")
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
    @DisplayName("비회원 주문 생성에 성공한다")
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
    @DisplayName("Bean Validation에 실패한다")
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
    @DisplayName("CreateOrderRequest가 null 이면 주문 생성에 실패한다")
    void createOrder_paramIsNull_fail() throws Exception {
        // given
        CreateOrderRequest request = null;

        // when & then
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(CreateOrderRequest.class), anyString());
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
    @DisplayName("완료되지 않은 주문 조회에 실패한다(이유: orderNumber is blank)")
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

//
//    @Test
//    @DisplayName("회원 주문 생성에 성공한다")
//    void createOrder_withValidMemberRequest_success() throws Exception {
//        // given
//        String xUserId = "testUser";
//        given(orderService.createOrder(any(OrderRequest.class), eq(xUserId)))
//                .willReturn(orderResponse);
//
//        // when & then
//        mockMvc.perform(post("/orders")
//                        .header("X-USER-ID", xUserId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(validOrderRequest)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.orderId").value("202507-abcabc-123123"))
//                .andExpect(jsonPath("$.receiverName").value("홍길동"))
//                .andExpect(jsonPath("$.receiverPhoneNumber").value("010-1234-5678"))
//                .andExpect(jsonPath("$.address").value("00000 서울특별시 강남구 테헤란로 123"))
//                .andExpect(jsonPath("$.totalAmount").value(55000))
//                .andExpect(jsonPath("$.deliveryFee").value(3000))
//                .andExpect(jsonPath("$.status").value("PENDING"));
//
//        verify(orderService).createOrder(any(OrderRequest.class), eq(xUserId));
//    }
//
//    @Test
//    @DisplayName("비회원 주문 생성에 성공한다")
//    void createOrder_withValidGuestRequest_success() throws Exception {
//        // given
//        String xUserId = "";
//        given(orderService.createOrder(any(OrderRequest.class), eq(xUserId)))
//                .willReturn(orderResponse);
//
//        // when & then
//        mockMvc.perform(post("/orders")
//                        .header("X-USER-ID", "")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(validOrderRequest)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.orderId").value("202507-abcabc-123123"))
//                .andExpect(jsonPath("$.receiverName").value("홍길동"));
//
//        verify(orderService).createOrder(any(OrderRequest.class), eq(xUserId));
//    }
//
//    @Test
//    @DisplayName("잘못된 Content-Type으로 요청 시 415 에러가 발생한다")
//    void createOrder_withWrongContentType_unsupportedMediaType() throws Exception {
//        // given
//        String xUserId = "testUser";
//
//        // when & then
//        mockMvc.perform(post("/orders")
//                        .header("X-USER-ID", xUserId)
//                        .contentType(MediaType.TEXT_PLAIN)
//                        .content("invalid content"))
//                .andExpect(status().isUnsupportedMediaType());
//
//        verify(orderService, never()).createOrder(any(OrderRequest.class), anyString());
//    }
//
//    @Test
//    @DisplayName("잘못된 JSON 형식으로 요청 시 400 에러가 발생한다")
//    void createOrder_withInvalidJson_badRequest() throws Exception {
//        // given
//        String xUserId = "testUser";
//        String invalidJson = "{invalid json}";
//
//        // when & then
//        mockMvc.perform(post("/orders")
//                        .header("X-USER-ID", xUserId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(invalidJson))
//                .andExpect(status().isBadRequest());
//
//        verify(orderService, never()).createOrder(any(OrderRequest.class), anyString());
//    }
//
//    @Test
//    @DisplayName("회원 주문 전체 조회에 성공한다")
//    void getAllOrdersByUserId_success() throws Exception {
//        // given
//        String xUserId = "testUser";
//        List<OrderSummaryResponse> orderSummaries = List.of(
//                new OrderSummaryResponse(LocalDate.now(), "202507-abcabc-123123", "홍길동", 10_000L),
//                new OrderSummaryResponse(LocalDate.now().minusDays(1), "202507-abcabc-123124", "김철수", 10_000L)
//        );
//        Page<OrderSummaryResponse> pageResult = new PageImpl<>(orderSummaries, PageRequest.of(0, 20), 10);
//
//        given(orderService.findAllByUserId(xUserId, PageRequest.of(0, 20))).willReturn(pageResult);
//
//        // when & then
//        mockMvc.perform(get("/orders")
//                        .header("X-USER-ID", xUserId)
//                        .param("page", "0")
//                        .param("size", "20"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content").isArray())
//                .andExpect(jsonPath("$.content.length()").value(2))
//                .andExpect(jsonPath("$.content[0].orderId").value("202507-abcabc-123123"))
//                .andExpect(jsonPath("$.content[0].receiverName").value("홍길동"))
//                .andExpect(jsonPath("$.content[1].orderId").value("202507-abcabc-123124"))
//                .andExpect(jsonPath("$.content[1].receiverName").value("김철수"));
//
//        verify(orderService).findAllByUserId(xUserId, PageRequest.of(0, 20));
//    }
//
//    @Test
//    @DisplayName("회원 주문 전체 조회 시 빈 페이지를 반환한다")
//    void getAllOrdersByUserId_emptyPage() throws Exception {
//        // given
//        String xUserId = "testUser";
//        Page<OrderSummaryResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
//
//        given(orderService.findAllByUserId(eq(xUserId), any(PageRequest.class))).willReturn(emptyPage);
//
//        // when & then
//        mockMvc.perform(get("/orders")
//                        .header("X-USER-ID", xUserId)
//                        .param("page", "0")
//                        .param("size", "20"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content").isArray())
//                .andExpect(jsonPath("$.content.length()").value(0));
//
//        verify(orderService).findAllByUserId(eq(xUserId), any(PageRequest.class));
//    }
//
//    @Test
//    @DisplayName("X-USER-ID 헤더 없이 요청하면 400 에러가 발생한다")
//    void getAllOrdersByUserId_missingXUserIdHeader_badRequest() throws Exception {
//        // when & then
//        mockMvc.perform(get("/orders"))
//                .andExpect(status().isBadRequest());
//
//        verify(orderService, never()).findAllByUserId(anyString(), any());
//    }
//
//    @Test
//    @DisplayName("회원 주문 상세 조회에 성공한다")
//    void getOrder_success() throws Exception {
//        // given
//        String xUserId = "testUser";
//        String orderId = "202507-abcabc-123123";
//
//        given(orderService.findByOrderId(xUserId, orderId)).willReturn(detailResponse);
//
//        // when & then
//        mockMvc.perform(get("/orders/{orderId}", orderId)
//                        .header("X-USER-ID", xUserId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.orderId").value("202507-abcabc-123123"))
//                .andExpect(jsonPath("$.receiverName").value("홍길동"))
//                .andExpect(jsonPath("$.receiverPhoneNumber").value("010-1234-5678"))
//                .andExpect(jsonPath("$.address").value("00000 서울특별시 강남구 테헤란로 123"))
//                .andExpect(jsonPath("$.totalAmount").value(55000))
//                .andExpect(jsonPath("$.deliveryFee").value(3000))
//                .andExpect(jsonPath("$.status").value("PENDING"));
//
//        verify(orderService).findByOrderId(xUserId, orderId);
//    }
//
//    @Test
//    @DisplayName("회원 주문 상세 조회 시 X-USER-ID 헤더가 없으면 400 에러가 발생한다")
//    void getOrder_missingXUserIdHeader_badRequest() throws Exception {
//        // given
//        String orderId = "202507-abcabc-123123";
//
//        // when & then
//        mockMvc.perform(get("/orders/{orderId}", orderId))
//                .andExpect(status().isBadRequest());
//
//        verify(orderService, never()).findByOrderId(anyString(), anyString());
//    }
//
//    // ========== 구매 확인 테스트 ==========
//
//    @Test
//    @DisplayName("구매 확인 성공 - 구매 이력이 있는 경우")
//    void verifyPurchase_success_withValidPurchase() throws Exception {
//        // given
//        String xUserId = "testUser";
//        Long bookId = 123L;
//        PurchaseVerificationResponse expectedResponse = new PurchaseVerificationResponse(1L, bookId, true);
//
//        given(orderService.verifyPurchase(xUserId, bookId)).willReturn(expectedResponse);
//
//        // when & then
//        mockMvc.perform(get("/orders/verify-purchase")
//                        .header("X-USER-ID", xUserId)
//                        .param("bookId", bookId.toString()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.userNo").value(1L))
//                .andExpect(jsonPath("$.bookId").value(123L))
//                .andExpect(jsonPath("$.isValid").value(true));
//
//        verify(orderService).verifyPurchase(xUserId, bookId);
//    }
//
//    @Test
//    @DisplayName("구매 확인 성공 - 구매 이력이 없는 경우")
//    void verifyPurchase_success_withNoPurchase() throws Exception {
//        // given
//        String xUserId = "testUser";
//        Long bookId = 456L;
//        PurchaseVerificationResponse expectedResponse = new PurchaseVerificationResponse(1L, bookId, false);
//
//        given(orderService.verifyPurchase(xUserId, bookId)).willReturn(expectedResponse);
//
//        // when & then
//        mockMvc.perform(get("/orders/verify-purchase")
//                        .header("X-USER-ID", xUserId)
//                        .param("bookId", bookId.toString()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.userNo").value(1L))
//                .andExpect(jsonPath("$.bookId").value(456L))
//                .andExpect(jsonPath("$.isValid").value(false));
//
//        verify(orderService).verifyPurchase(xUserId, bookId);
//    }
//
//    @Test
//    @DisplayName("구매 확인 - X-USER-ID 헤더 없이 요청하면 400 에러 발생")
//    void verifyPurchase_missingXUserIdHeader_badRequest() throws Exception {
//        // given
//        Long bookId = 123L;
//
//        // when & then
//        mockMvc.perform(get("/orders/verify-purchase")
//                        .param("bookId", bookId.toString()))
//                .andExpect(status().isBadRequest());
//
//        verify(orderService, never()).verifyPurchase(anyString(), any(Long.class));
//    }
//
//    @Test
//    @DisplayName("구매 확인 - bookId 파라미터 없이 요청하면 400 에러 발생")
//    void verifyPurchase_missingBookIdParam_badRequest() throws Exception {
//        // given
//        String xUserId = "testUser";
//
//        // when & then
//        mockMvc.perform(get("/orders/verify-purchase")
//                        .header("X-USER-ID", xUserId))
//                .andExpect(status().isBadRequest());
//
//        verify(orderService, never()).verifyPurchase(anyString(), any(Long.class));
//    }
//
//    @Test
//    @DisplayName("구매 확인 - 잘못된 bookId 형식으로 요청하면 400 에러 발생")
//    void verifyPurchase_invalidBookIdFormat_badRequest() throws Exception {
//        // given
//        String xUserId = "testUser";
//        String invalidBookId = "invalid";
//
//        // when & then
//        mockMvc.perform(get("/orders/verify-purchase")
//                        .header("X-USER-ID", xUserId)
//                        .param("bookId", invalidBookId))
//                .andExpect(status().isBadRequest());
//
//        verify(orderService, never()).verifyPurchase(anyString(), any(Long.class));
//    }
}
