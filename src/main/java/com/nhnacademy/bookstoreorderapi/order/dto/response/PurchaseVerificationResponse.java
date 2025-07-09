package com.nhnacademy.bookstoreorderapi.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PurchaseVerificationResponse {

    private Long userNo;
    private Long bookId;
    private Boolean isValid;
}
