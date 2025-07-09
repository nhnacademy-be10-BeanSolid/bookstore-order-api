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

    /** 샌드박스 또는 프로덕션 API 엔드포인트 */
    private String baseUrl;

    /** 샌드박스 클라이언트 키 */
    private String clientApiKey;

    /** 샌드박스 시크릿 키 */
    private String secretApiKey;

    /** 결제 성공 콜백 URL */
    private String successUrl;

    /** 결제 실패 콜백 URL */
    private String failUrl;

    /**
     * Basic 인증 헤더 생성 (secretApiKey: 로 구성)
     * @return "Basic {base64(secretApiKey + ':')}"
     */
    public String getBasicAuthHeader() {
        String creds = secretApiKey + ":";
        String encoded = Base64
                .getEncoder()
                .encodeToString(creds.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}