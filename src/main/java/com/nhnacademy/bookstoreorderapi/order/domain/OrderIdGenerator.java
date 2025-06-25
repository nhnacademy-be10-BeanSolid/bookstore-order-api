package com.nhnacademy.bookstoreorderapi.order.domain;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 주문번호 생성 클래스.
 *
 * <p>이 클래스는 다음 형식의 주문번호를 생성합니다:
 * <pre>{주문이 생성된 연월(YYYYMM)}-{알파벳 대소문자 및 숫자 중 6자리}-{알파벳 대소문자 및 숫자 중 6자리}</pre>
 * 예) 202506-4fG7hK-9Lm2Pq</p>
 */
public class OrderIdGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final int SEGMENT_LENGTH = 6;
    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                            + "abcdefghijklmnopqrstuvwxyz"
                                            + "0123456789";

    public static String generate() {

        String yearMonth = nowYearMonth();
        String segment1 = randomSegment();
        String segment2 = randomSegment();

        return String.format("%s-%s-%s", yearMonth, segment1, segment2);
    }

    private static String randomSegment() {

        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom(); // 보안을 위해 SecureRandom을 사용.
        for (int i = 0; i < 6; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }

        return sb.toString();
    }

    private static String nowYearMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }
}
