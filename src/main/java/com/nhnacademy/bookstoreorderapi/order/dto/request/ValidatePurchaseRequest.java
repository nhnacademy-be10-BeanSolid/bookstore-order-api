package com.nhnacademy.bookstoreorderapi.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ValidatePurchaseRequest(

        @NotNull @Positive
        Long bookId,

        @NotNull @Positive
        Long userNo
) {
}
