package com.nhnacademy.bookstoreorderapi.payment.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class TossFeignConfig {

    private final TossPaymentConfig props;

    @Bean
    public RequestInterceptor tossAuthInterceptor() {

        return (RequestTemplate template) -> {
            template.header("Authorization", props.getBasicAuthHeader());
            template.header("Content-Type", "application/json");
            template.header("User-Agent", "BookstoreOrderApi/1.0");


            String url = template.url();
            String method = template.method();

            if ("POST".equals(method) && "/payments".equals(url)) {
                template.header("X-Client-Api-Key", props.getClientApiKey());
            }
        };
    }
}