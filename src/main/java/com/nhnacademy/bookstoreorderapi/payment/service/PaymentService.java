package com.nhnacademy.bookstoreorderapi.payment.service;

import com.nhnacademy.bookstoreorderapi.payment.dto.Request.CancelPaymentRequest;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentApprovalRequestDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Request.PaymentReqDto;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;

import java.util.Map;

public interface PaymentService {

    PaymentResDto requestTossPayment(String orderNumber, PaymentReqDto dto);

    PaymentApprovalRequestDto markSuccess(PaymentApprovalRequestDto dto);

    void markFail(String paymentKey, String failMessage);

    PaymentResDto getPaymentInfo(String paymentKey);

    PaymentResDto refundCardPayment(String paymentKey, CancelPaymentRequest  request);
}