package com.nhnacademy.bookstoreorderapi.payment.service;

import com.nhnacademy.bookstoreorderapi.payment.dto.Request.CancelPaymentRequest;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentApprovalRequestDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 요청 인터페이스 호출")
    void testRequestTossPayment() {
        when(paymentService.requestTossPayment(anyString(), any(PaymentReqDto.class)))
                .thenReturn(new PaymentResDto());

        assertDoesNotThrow(() -> paymentService.requestTossPayment("orderId", new PaymentReqDto()));
    }

    @Test
    @DisplayName("결제 성공 처리 인터페이스 호출")
    void testMarkSuccess() {
        when(paymentService.markSuccess(any(PaymentApprovalRequestDto.class)))
                .thenReturn(new PaymentApprovalRequestDto("paymentKey", "orderId", 1000L));

        assertDoesNotThrow(() -> paymentService.markSuccess(new PaymentApprovalRequestDto("paymentKey", "orderId", 1000L)));
    }

    @Test
    @DisplayName("결제 실패 처리 인터페이스 호출")
    void testMarkFail() {
        doNothing().when(paymentService).markFail(anyString(), anyString());

        assertDoesNotThrow(() -> paymentService.markFail("paymentKey", "reason"));
    }

    @Test
    @DisplayName("카드 결제 환불 인터페이스 호출")
    void testRefundCardPayment() {
        when(paymentService.refundCardPayment(anyString(), any(CancelPaymentRequest.class)))
                .thenReturn(new PaymentResDto());

        assertDoesNotThrow(() -> paymentService.refundCardPayment("paymentKey", new CancelPaymentRequest()));
    }

    @Test
    @DisplayName("결제 정보 조회 인터페이스 호출")
    void testGetPaymentInfo() {
        when(paymentService.getPaymentInfo(anyString()))
                .thenReturn(new PaymentResDto());

        assertDoesNotThrow(() -> paymentService.getPaymentInfo("paymentKey"));
    }
}
