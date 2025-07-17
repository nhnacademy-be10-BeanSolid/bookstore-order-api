package com.nhnacademy.bookstoreorderapi.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.CancelPaymentRequest;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentApprovalRequestDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class)
@ActiveProfiles("test")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_ORDER_ID = "testOrderId";
    private static final String TEST_PAYMENT_KEY = "testPaymentKey";
    private static final Long TEST_AMOUNT = 10000L;
    private static final String TEST_PAYMENT_NAME = "테스트 결제";

    private PaymentReqDto paymentReqDto;
    private PaymentResDto paymentResDto;

    @BeforeEach
    void setUp() {
        paymentReqDto = new PaymentReqDto();
        paymentReqDto.setPayType(PayType.CARD);
        paymentReqDto.setPayName(TEST_PAYMENT_NAME);
        paymentReqDto.setPayAmount(TEST_AMOUNT);

        paymentResDto = new PaymentResDto();
        paymentResDto.setPaymentKey(TEST_PAYMENT_KEY);
        paymentResDto.setOrderId(TEST_ORDER_ID);
        paymentResDto.setPayAmount(TEST_AMOUNT);
        paymentResDto.setPayName(TEST_PAYMENT_NAME);
    }

    @Test
    @DisplayName("JSON 바디로 Toss 결제 요청")
    void requestPayment_withJsonBody_returnsCreated() throws Exception {
        given(paymentService.requestTossPayment(anyString(), any(PaymentReqDto.class))).willReturn(paymentResDto);

        mockMvc.perform(post("/api/v1/payments/toss/{orderId}", TEST_ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentReqDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/payments/" + TEST_PAYMENT_KEY))
                .andExpect(jsonPath("$.paymentKey").value(TEST_PAYMENT_KEY))
                .andExpect(jsonPath("$.orderId").value(TEST_ORDER_ID));

        verify(paymentService).requestTossPayment(eq(TEST_ORDER_ID), any(PaymentReqDto.class));
    }

    @Test
    @DisplayName("GET 방식으로 Toss 결제 요청")
    void requestPayment_withQueryParams_returnsCreated() throws Exception {
        given(paymentService.requestTossPayment(anyString(), any(PaymentReqDto.class))).willReturn(paymentResDto);

        mockMvc.perform(get("/api/v1/payments/toss/{orderId}/create", TEST_ORDER_ID)
                        .param("payType", PayType.CARD.name())
                        .param("payName", TEST_PAYMENT_NAME)
                        .param("payAmount", String.valueOf(TEST_AMOUNT)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/payments/" + TEST_PAYMENT_KEY))
                .andExpect(jsonPath("$.paymentKey").value(TEST_PAYMENT_KEY));

        verify(paymentService).requestTossPayment(eq(TEST_ORDER_ID), any(PaymentReqDto.class));
    }

    @Test
    @DisplayName("결제 정보 조회")
    void getPaymentInfo_returnsPaymentInfo() throws Exception {
        given(paymentService.getPaymentInfo(anyString())).willReturn(paymentResDto);

        mockMvc.perform(get("/api/v1/payments/{paymentKey}", TEST_PAYMENT_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentKey").value(TEST_PAYMENT_KEY))
                .andExpect(jsonPath("$.orderId").value(TEST_ORDER_ID));

        verify(paymentService).getPaymentInfo(eq(TEST_PAYMENT_KEY));
    }

    @Test
    @DisplayName("Toss 결제 성공 콜백")
    void tossSuccess_redirectsToSuccessPage() throws Exception {
        PaymentApprovalRequestDto approvalDto = new PaymentApprovalRequestDto(TEST_PAYMENT_KEY, TEST_ORDER_ID, TEST_AMOUNT);
        given(paymentService.markSuccess(any(PaymentApprovalRequestDto.class))).willReturn(approvalDto);

        mockMvc.perform(get("/api/v1/payments/success")
                        .param("paymentKey", TEST_PAYMENT_KEY)
                        .param("orderId", TEST_ORDER_ID)
                        .param("amount", String.valueOf(TEST_AMOUNT)))
                .andExpect(status().isOk());

        verify(paymentService).markSuccess(any(PaymentApprovalRequestDto.class));
    }

    @Test
    @DisplayName("Toss 결제 실패 콜백")
    void tossFail_redirectsToFailPage() throws Exception {
        String errorMessage = "결제 실패 메시지";
        mockMvc.perform(get("/api/v1/payments/fail")
                        .param("paymentKey", TEST_PAYMENT_KEY)
                        .param("orderId", TEST_ORDER_ID)
                        .param("message", errorMessage))
                .andExpect(status().is3xxRedirection());

        verify(paymentService).markFail(eq(TEST_PAYMENT_KEY), eq(errorMessage));
    }

    @Test
    @DisplayName("결제 취소(환불) 요청")
    void cancelPayment_returnsOk() throws Exception {
        CancelPaymentRequest cancelRequest = new CancelPaymentRequest(TEST_ORDER_ID, TEST_AMOUNT, "테스트 취소");
        given(paymentService.refundCardPayment(anyString(), any(CancelPaymentRequest.class))).willReturn(paymentResDto);

        mockMvc.perform(post("/api/v1/payments/{paymentKey}/cancel", TEST_PAYMENT_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentKey").value(TEST_PAYMENT_KEY));

        verify(paymentService).refundCardPayment(eq(TEST_PAYMENT_KEY), any(CancelPaymentRequest.class));
    }
}
