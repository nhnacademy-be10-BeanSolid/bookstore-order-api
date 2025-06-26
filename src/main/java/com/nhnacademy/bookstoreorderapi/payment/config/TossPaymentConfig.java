package com.nhnacademy.bookstoreorderapi.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <h2>TossPayments 환경 설정</h2>
 * <p>
 *     <ul>
 *         <li>yml 위치 : <code>payment.toss.*</code></li>
 *         <li>속성명이 필드명(kebab-case)과 1:1로 매핑됩니다.</li>
 *         <li><code>sandbox=true</code> 면 <code>test_*</code> 키가, <code>false</code> 면 <code>live_*</code> 키가 자동 선택됩니다.</li>
 *     </ul>
 * </p>
 */
@Configuration
@ConfigurationProperties(prefix = "payment.toss")
@Getter
@Setter
public class TossPaymentConfig {

    /**
     * <b>true</b> : 테스트 결제 모드 (Toss Payments 콘솔의 <code>test_*</code> 키 사용)<br>
     * <b>false</b> : 실 결제 모드 ( <code>live_*</code> 키 사용 )
     */
    private boolean sandbox = true;

    /* ========================= 테스트 키 ========================= */
    private String testClientApiKey;   // payment.toss.test-client-api-key
    private String testSecretApiKey;   // payment.toss.test-secret-api-key

    /* ========================= 라이브 키 ========================= */
    private String liveClientApiKey;   // payment.toss.live-client-api-key
    private String liveSecretApiKey;   // payment.toss.live-secret-api-key

    /* ========================== 콜백 URL ========================= */
    private String successUrl;         // payment.toss.success-url
    private String failUrl;            // payment.toss.fail-url

    /* ------------------------------------------------------------ */

    /** 현재 환경(client) 키 */
    public String getClientKey() {
        return sandbox ? testClientApiKey : liveClientApiKey;
    }

    /** 현재 환경(secret) 키 */
    public String getSecretKey() {
        return sandbox ? testSecretApiKey : liveSecretApiKey;
    }

    /* ======================= End-Point 상수 ===================== */

    /** 기본 결제 요청 URL */
    public static final String PAYMENTS_URL = "https://api.tosspayments.com/v1/payments";

    /**
     * 결제 확정(approve) URL
     * @param paymentKey Toss 가 내려준 <code>paymentKey</code>
     */
    public static String confirmUrl(String paymentKey) {
        return PAYMENTS_URL + "/" + paymentKey + "/confirm";
    }

    /**
     * 결제 취소(cancel) URL
     * @param paymentKey Toss 가 내려준 <code>paymentKey</code>
     */
    public static String cancelUrl(String paymentKey) {
        return PAYMENTS_URL + "/" + paymentKey + "/cancel";
    }
}
