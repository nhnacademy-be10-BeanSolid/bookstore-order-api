package com.nhnacademy.bookstoreorderapi.order.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnRequestDto {

    private String reason;
    private LocalDateTime requestedAt;
    private Boolean damaged;
}
