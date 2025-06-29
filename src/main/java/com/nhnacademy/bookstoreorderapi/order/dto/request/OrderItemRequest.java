package com.nhnacademy.bookstoreorderapi.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(

    @NotNull
    @Positive
    Long bookId,

    @NotNull
    @Positive
    Integer quantity,

    @Positive
    Long wrappingId
) {}