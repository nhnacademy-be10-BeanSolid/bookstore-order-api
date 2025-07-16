package com.nhnacademy.bookstoreorderapi.payment.dto.Response;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@EqualsAndHashCode
@ToString
public class ErrorResponse {
    private final int status;
    private final String code;
    private final String message;
}