// src/main/java/com/nhnacademy/bookstoreorderapi/order/dto/ErrorResponseDto.java
package com.nhnacademy.bookstoreorderapi.order.dto;

import lombok.AllArgsConstructor;
import lombok.*;


@Getter
@AllArgsConstructor
public class ErrorResponseDto implements ResponseDto {
    private final String error;
}
