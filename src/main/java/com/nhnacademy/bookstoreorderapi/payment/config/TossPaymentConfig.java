package com.nhnacademy.bookstoreorderapi.payment.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "payment.toss")
public class TossPaymentConfig {

    /** application.yml → payment.toss. 받아온다!!* */
    private String testClientApiKey;
    private String testSecretApiKey;
    private String successUrl;
    private String failUrl;

    public static final String PAYMENTS_URL = "https://api.tosspayments.com/v1/payments/";

    /* setter 는 ConfigurationProperties 바인딩용(필수) */
    public void setTestClientApiKey(String key)  {
        this.testClientApiKey  = key;
    }
    public void setTestSecretApiKey(String key) {
        this.testSecretApiKey = key;
    }
    public void setSuccessUrl(String url)       {
        this.successUrl       = url;
    }
    public void setFailUrl(String url)          {
        this.failUrl          = url;
    }
}