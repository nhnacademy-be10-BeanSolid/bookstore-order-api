package com.nhnacademy.bookstoreorderapi.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "payment.toss")
public class TossPaymentConfig {
    private boolean sandbox;
    private String testClientApiKey;
    private String testSecretApiKey;
    private String successUrl;
    private String failUrl;
    public static final String PAYMENTS_URL = "https://api.tosspayments.com/v1/payments/";
}