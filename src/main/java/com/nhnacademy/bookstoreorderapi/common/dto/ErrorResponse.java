package com.nhnacademy.bookstoreorderapi.common.dto;

public record ErrorResponse(
        String errorCode,
        String errorMessage
) {}
