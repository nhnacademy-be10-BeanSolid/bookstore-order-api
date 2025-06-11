package com.nhnacademy.bookstoreorderapi.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponseDto implements ResponseDto {

    private final String error;
}
