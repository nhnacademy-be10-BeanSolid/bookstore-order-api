package com.nhnacademy.bookstoreorderapi.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {
    @NotNull
    @Positive
    private Long bookId;

    @NotNull @Min(1)
    private Integer quantity;

    @Positive
    private Long wrappingId;
}