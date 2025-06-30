package com.nhnacademy.bookstoreorderapi.payment.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class TossFeignConfig {
    private final TossPaymentConfig tossConfig;

    @Bean
    public RequestInterceptor tossAuthInterceptor() {
        return template -> {
            template.header("Authorization", tossConfig.getBasicAuthHeader());
            template.header("X-Client-Api-Key", tossConfig.getClientApiKey());
        };
    }
}