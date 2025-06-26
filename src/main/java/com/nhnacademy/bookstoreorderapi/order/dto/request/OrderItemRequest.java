package com.nhnacademy.bookstoreorderapi.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
    @NotNull
    @Positive
    Long bookId,

    @NotNull @Min(1)
    Integer quantity,

    @Positive
    Long wrappingId
) {}