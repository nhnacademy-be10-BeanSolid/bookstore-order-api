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


// 단위test
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @MockBean
    PaymentService paymentService;   // Service 는 목

    @Test
    @DisplayName("POST /api/v1/payments/toss/{orderId} → 201 Created")
    void requestPayment_api() throws Exception {
        PaymentResDto stub = PaymentResDto.builder()
                .paymentId(1L)
                .orderId("24RGRF9fEQWEDV222345")
                .paymentKey("paymentkey39485")
                .redirectUrl("https://redirect")
                .successUrl("ok")
                .failUrl("fail")
                .payName("도서 결제")
                .payAmount(5_000L)
                .payType("CARD")
                .build();
        when(paymentService.requestTossPayment(any(), any())).thenReturn(stub);

        PaymentReqDto req = PaymentReqDto.builder()
                .payAmount(5_000L)
                .payType(PayType.CARD)
                .payName("도서 결제")
                .build();

        mvc.perform(post("/api/v1/payments/toss/{oid}", "ORD-10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/payments/1"))
                .andExpect(jsonPath("$.paymentKey").value("pay_999"));
    }
}