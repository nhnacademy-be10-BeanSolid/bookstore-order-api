package com.nhnacademy.bookstoreorderapi.payment.dto.toss;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class PaymentCreateResponse {
    private String paymentKey;

}
