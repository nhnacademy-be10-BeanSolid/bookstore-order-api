// src/main/java/com/nhnacademy/bookstoreorderapi/payment/config/TossFeignConfig.java
package com.nhnacademy.bookstoreorderapi.payment.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TossFeignConfig {
    private final TossPaymentConfig props;

    public TossFeignConfig(TossPaymentConfig props) {
        this.props = props;
    }

    @Bean
    public RequestInterceptor tossAuthInterceptor() {
        return template -> {
            template.header("Authorization", props.getBasicAuthHeader());
            template.header("X-Client-Api-Key", props.getClientApiKey());
        };
    }
}