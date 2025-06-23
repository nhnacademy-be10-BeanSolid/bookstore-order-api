package com.nhnacademy.bookstoreorderapi.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequestDto {
    @NotNull
    private Long bookId;

    @NotNull @Min(1)
    private Integer quantity;

    private Long wrappingId;
}