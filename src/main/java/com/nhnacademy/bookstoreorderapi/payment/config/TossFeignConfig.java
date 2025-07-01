package com.nhnacademy.bookstoreorderapi.payment.config;

import feign.RequestInterceptor;
import feign.okhttp.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.OkHttpClient.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

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
            template.header("Content-Type", "application/json");
            template.header("Accept", "application/json");            // ← 추가
            template.header("User-Agent", "BookstoreOrderApi/1.0");
        };
    }

    @Bean
    public OkHttpClient feignOkHttpClient() {
        okhttp3.OkHttpClient client = new Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .build();
        return new OkHttpClient(client);
    }
}