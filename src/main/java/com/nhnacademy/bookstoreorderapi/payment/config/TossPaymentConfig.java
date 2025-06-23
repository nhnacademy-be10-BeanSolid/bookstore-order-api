package com.nhnacademy.bookstoreorderapi.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Getter
@Setter
@Primary
@Configuration
@ConfigurationProperties(prefix = "payment.toss")
public class TossPaymentConfig {
    private boolean sandbox;
    private String testClientApiKey;
    private String testSecretApiKey;
    private String successUrl;
    private String failUrl;

    // 실제 호출엔드포인트
    public static final String PAYMENTS_URL = "https://api.tosspayments.com/v1/payments";
}