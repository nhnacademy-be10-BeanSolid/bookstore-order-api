package com.nhnacademy.bookstoreorderapi.payment.dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class CancelPaymentRequest {
    @NotBlank
    private String orderId;

    @Min(1)
    private long amount;

    @NotBlank
    private String cancelReason;
}