package com.nhnacademy.bookstoreorderapi.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.extern.java.Log;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSuccessDto {

    @NotBlank(message = "paymentKey는 필수 입니다.")
    private String paymentId;

    @NotBlank(message = "orderId는 필수 입니다.")
    private Long orderId;

    @NotBlank(message = "amount는 필수입니다.")
    private Long amount;

}
