package com.nhnacademy.bookstoreorderapi.payment.service;

import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;

import java.util.Map;

public interface PaymentService {

     //Toss 결제 요청: DB 저장 → Toss API 호출 → paymentKey 저장 → DTO 반환
    PaymentResDto requestTossPayment(String orderId, PaymentReqDto dto);

     //결제 성공 콜백 처리
    void markSuccess(String paymentKey, String orderId, long amount);

     // 결제 실패 콜백 처리
    void markFail(String paymentKey, String failMessage);

     //포인트 환불(취소) 처리
    Map<String, Object> cancelPaymentPoint(String paymentKey, String cancelReason);

}