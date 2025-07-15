package com.nhnacademy.bookstoreorderapi.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReturnsRequest(

        @NotBlank
        String memo,

        @NotNull
        Boolean damaged
) {}