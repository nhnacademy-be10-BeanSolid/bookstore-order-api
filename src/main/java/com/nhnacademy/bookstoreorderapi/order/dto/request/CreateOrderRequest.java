package com.nhnacademy.bookstoreorderapi.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateOrderRequest(

        @NotEmpty(message = "구매할 상품을 추가해주세요(현재: 구매할 상품 없음)")
        @Valid
        List<CreateOrderItemRequest> createItemRequests
) {
    public record CreateOrderItemRequest(

            @NotNull @Positive
            Long bookId,

            @NotNull @Positive
            Integer quantity
    ) {}
}
