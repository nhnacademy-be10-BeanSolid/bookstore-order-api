package com.nhnacademy.bookstoreorderapi.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.CancelPaymentRequest;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentReqDto paymentReqDto;
    private PaymentResDto paymentResDto;
    private CancelPaymentRequest cancelPaymentRequest;

    @BeforeEach
    void setUp() {
        paymentReqDto = new PaymentReqDto();
        paymentReqDto.setPayType(PayType.CARD);
        paymentReqDto.setPayName("테스트 결제");
        paymentReqDto.setPayAmount(10000L);

        paymentResDto = new PaymentResDto();
        paymentResDto.setPaymentKey("testPaymentKey");
        paymentResDto.setOrderId("testOrderId");
        paymentResDto.setPayAmount(10000L);

        cancelPaymentRequest = new CancelPaymentRequest();
        cancelPaymentRequest.setCancelReason("테스트 취소");
    }

    @Test
    @DisplayName("(1) JSON 바디로 Toss 결제 요청 테스트")
    void requestPaymentTest() throws Exception {
        given(paymentService.requestTossPayment(anyString(), any(PaymentReqDto.class)))
                .willReturn(paymentResDto);

        mockMvc.perform(post("/api/v1/payments/toss/{orderId}", "testOrderId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentReqDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/payments/testPaymentKey"))
                .andExpect(jsonPath("$.paymentKey").value("testPaymentKey"));

        verify(paymentService).requestTossPayment(eq("testOrderId"), any(PaymentReqDto.class));
    }

    @Test
    @DisplayName("(2) GET 방식으로 Toss 결제 요청 (query parameter) 테스트")
    void requestPaymentViaGetTest() throws Exception {
        given(paymentService.requestTossPayment(anyString(), any(PaymentReqDto.class)))
                .willReturn(paymentResDto);

        mockMvc.perform(get("/api/v1/payments/toss/{orderId}/create", "testOrderId")
                        .param("payType", PayType.CARD.name())
                        .param("payName", "테스트 결제")
                        .param("payAmount", "10000"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/payments/testPaymentKey"))
                .andExpect(jsonPath("$.paymentKey").value("testPaymentKey"));

        verify(paymentService).requestTossPayment(eq("testOrderId"), any(PaymentReqDto.class));
    }

    @Test
    @DisplayName("(3) 결제 정보 조회 테스트")
    void getPaymentInfoTest() throws Exception {
        given(paymentService.getPaymentInfo(anyString()))
                .willReturn(paymentResDto);

        mockMvc.perform(get("/api/v1/payments/{paymentKey}", "testPaymentKey"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentKey").value("testPaymentKey"));

        verify(paymentService).getPaymentInfo(eq("testPaymentKey"));
    }

    @Test
    @DisplayName("(4) Toss 성공 콜백 테스트")
    void tossSuccessTest() throws Exception {
        org.springframework.util.MultiValueMap<String, String> params = new org.springframework.util.LinkedMultiValueMap<>();
        params.add("paymentKey", "testPaymentKey");
        params.add("orderId", "testOrderId");
        params.add("amount", "10000");

        mockMvc.perform(get("/api/v1/payments/toss/success")
                        .params(params))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/success.html?*"));

        verify(paymentService).markSuccess(eq("testPaymentKey"), eq("testOrderId"), eq(10000L));
    }

    @Test
    @DisplayName("(5) Toss 실패 콜백 테스트")
    void tossFailTest() throws Exception {
        org.springframework.util.MultiValueMap<String, String> params = new org.springframework.util.LinkedMultiValueMap<>();
        params.add("paymentKey", "testPaymentKey");
        params.add("message", "결제 실패 메시지");

        mockMvc.perform(get("/api/v1/payments/toss/fail")
                        .params(params))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/fail.html?*"));

        verify(paymentService).markFail(eq("testPaymentKey"), eq("결제 실패 메시지"));
    }

    @Test
    @DisplayName("(6) 카드 결제 환불 (레거시 Map 기반) 테스트")
    void refundCardPaymentLegacyTest() throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("cancelReason", "테스트 환불");
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "SUCCESS");

        given(paymentService.refundCardPayment(anyString(), any(Map.class)))
                .willReturn(resp);

        mockMvc.perform(post("/api/v1/payments/toss/{paymentKey}/refund", "testPaymentKey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(paymentService).refundCardPayment(eq("testPaymentKey"), any(Map.class));
    }

    @Test
    @DisplayName("(7) 결제 취소(환불) 요청 (프론트 Cancel 버튼) 테스트")
    void cancelPaymentTest() throws Exception {
        CancelPaymentRequest cancelPaymentRequest = new CancelPaymentRequest();
        cancelPaymentRequest.setOrderId("testOrderId");  //
        cancelPaymentRequest.setAmount(10000L);          //
        cancelPaymentRequest.setCancelReason("테스트 취소");

        given(paymentService.refundCardPayment(anyString(), any(CancelPaymentRequest.class)))
                .willReturn(paymentResDto);

        mockMvc.perform(post("/api/v1/payments/{paymentKey}/cancel", "testPaymentKey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelPaymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentKey").value("testPaymentKey"));

        verify(paymentService).refundCardPayment(eq("testPaymentKey"), any(CancelPaymentRequest.class));
    }
}
