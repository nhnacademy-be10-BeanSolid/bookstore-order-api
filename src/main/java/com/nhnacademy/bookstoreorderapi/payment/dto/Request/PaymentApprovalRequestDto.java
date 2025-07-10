package com.nhnacademy.bookstoreorderapi.payment.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentApprovalRequestDto {

    private String paymentKey;
    private String orderId;
    private long amount;
}
