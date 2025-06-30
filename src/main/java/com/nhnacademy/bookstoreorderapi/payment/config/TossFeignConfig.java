package com.nhnacademy.bookstoreorderapi.payment.config;

import feign.okhttp.OkHttpClient;
import okhttp3.Protocol;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TossFeignConfig {
    private final TossPaymentConfig props;

    public TossFeignConfig(TossPaymentConfig props) {
        this.props = props;
    }

    // 1) HTTP/1.1 전용 OkHttpClient 빈 등록
    @Bean
    public OkHttpClient feignClient() {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .protocols(List.of(Protocol.HTTP_1_1))
                .build();
        return new OkHttpClient(client);
    }

    // 2) 인증 헤더 인터셉터
    @Bean
    public feign.RequestInterceptor tossAuthInterceptor() {
        return template -> {
            template.header("Authorization", props.getBasicAuthHeader());
            template.header("X-Client-Api-Key", props.getClientApiKey());
        };
    }
}