package com.nhnacademy.bookstoreorderapi.payment.dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelPaymentRequest {
    @NotBlank
    private String orderId;

    @Min(1)
    private long amount;

    @NotBlank
    private String cancelReason;
}
