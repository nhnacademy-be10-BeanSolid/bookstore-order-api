package com.nhnacademy.bookstoreorderapi.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment.toss")
@Getter
@Setter
public class TossPaymentConfig {

    /* ---- 필드 ---- */
    private String secretApiKey;
    private String clientApiKey;
    private String successUrl;
    private String failUrl;

    /* ---- PaymentServiceImpl 이 사용하게 될 메서드 ---- */
    public String getSecretKey() {
        return secretApiKey;
    }
    public String getClientKey() {
        return clientApiKey;
    }

    public static final String PAYMENTS_URL =
            "https://api.tosspayments.com/v1/payments";
}