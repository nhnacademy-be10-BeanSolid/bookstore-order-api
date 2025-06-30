package com.nhnacademy.bookstoreorderapi.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@ConfigurationProperties(prefix = "payment.toss")
@Getter
@Setter
public class TossPaymentConfig {

    // application.yml의 payment.toss.base-url
    private String baseUrl;

    // application.yml의 payment.toss.client-api-key
    private String clientApiKey;

    // application.yml의 payment.toss.secret-api-key
    private String secretApiKey;

    // 결제 성공 콜백 URL
    private String successUrl;

    // 결제 실패 콜백 URL
    private String failUrl;

    // Toss API 호출 시 사용할 Basic 인증 헤더 값 생성
    public String getBasicAuthHeader() {
        String creds = secretApiKey + ":";
        String encoded = Base64
                .getEncoder()
                .encodeToString(creds.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}