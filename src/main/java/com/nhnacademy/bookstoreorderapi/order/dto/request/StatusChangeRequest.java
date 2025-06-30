package com.nhnacademy.bookstoreorderapi.order.dto.request;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record StatusChangeRequest(
    @NotNull
    OrderStatus newStatus,
    String memo
) {}