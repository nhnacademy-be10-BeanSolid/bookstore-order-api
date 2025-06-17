package com.nhnacademy.bookstoreorderapi.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayType {

    CARD("카드"),
    ACCOUNT("계좌");

    private final String description;
}