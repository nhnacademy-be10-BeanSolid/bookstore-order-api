package com.nhnacademy.bookstoreorderapi.payment.service;

import com.nhnacademy.bookstoreorderapi.payment.dto.Request.CancelPaymentRequest;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentApprovalRequestDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;

public interface PaymentService {

    PaymentResDto requestTossPayment(String orderNumber, PaymentReqDto dto);

    PaymentApprovalRequestDto markSuccess(PaymentApprovalRequestDto dto);

    void markFail(String paymentKey, String failMessage);

    PaymentResDto getPaymentInfo(String paymentKey);

    PaymentResDto refundCardPayment(String paymentKey, CancelPaymentRequest  request); // 사용하지 않을 예정
    
    PaymentResDto refundCardPaymentByOrderNumber(String orderNumber, String cancelReason);



}