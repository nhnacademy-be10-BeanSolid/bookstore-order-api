package com.nhnacademy.bookstoreorderapi.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.bookstoreorderapi.payment.domain.PayType;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


// 단위 테스트: Controller 레이어만 띄워서 검증
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockBean
    PaymentService paymentService;   // 실제 서비스 대신 Mock

    @Test
    @DisplayName("POST /api/v1/payments/toss/{orderId} → 201 Created + Location 헤더 + paymentKey 반환")
    void requestPayment_api() throws Exception {
        // 1. Stub PaymentResDto 생성 (paymentKey를 "pay_999"로 설정)
        PaymentResDto stub = PaymentResDto.builder()
                .paymentId(1L)
                .orderId("ORD-10")
                .paymentKey("pay_999")            // <-- 여기
                .redirectUrl("https://redirect")
                .successUrl("ok")
                .failUrl("fail")
                .payName("도서 결제")
                .payAmount(5_000L)
                .payType("CARD")
                .build();

        // 2. Mock PaymentService 동작 정의
        when(paymentService.requestTossPayment(any(), any())).thenReturn(stub);

        // 3. 요청용 DTO
        PaymentReqDto req = PaymentReqDto.builder()
                .payAmount(5_000L)
                .payType(PayType.CARD)
                .payName("도서 결제")
                .build();

        // 4. 수행 및 검증
        mvc.perform(post("/api/v1/payments/toss/{oid}", "ORD-10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())                                     // 201
                .andExpect(header().string("Location", "/api/v1/payments/1"))         // Location 헤더
                .andExpect(jsonPath("$.paymentKey").value("pay_999"));                // 반환된 paymentKey
    }
}