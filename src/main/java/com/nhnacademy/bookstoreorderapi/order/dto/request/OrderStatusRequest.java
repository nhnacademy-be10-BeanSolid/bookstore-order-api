package com.nhnacademy.bookstoreorderapi.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderStatusRequest(

        @NotNull
        OrderAction action,

        @NotBlank
        String reason,

        Boolean damaged
) {
    public enum OrderAction {
        CANCEL,
        RETURN
    }
}