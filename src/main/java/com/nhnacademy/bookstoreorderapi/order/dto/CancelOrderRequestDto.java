package com.nhnacademy.bookstoreorderapi.order.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelOrderRequestDto {
    private String reason;
}
