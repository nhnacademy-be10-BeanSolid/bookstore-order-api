// src/main/java/com/nhnacademy/bookstoreorderapi/order/dto/ErrorResponseDto.java
package com.nhnacademy.bookstoreorderapi.order.dto;

import lombok.*;


@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto implements ResponseDto {
    private String error;
}