package com.nhnacademy.bookstoreorderapi.order.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 주문번호 생성 클래스.
 *
 * <p>이 클래스는 다음 형식의 주문번호를 생성합니다:
 * <pre>{주문이 생성된 연월(YYYYMM)}-{알파벳 대소문자 및 숫자 중 6자리}-{알파벳 대소문자 및 숫자 중 6자리}</pre>
 * 예) 202506-4fG7hK-9Lm2Pq</p>
 */
public class OrderIdGenerator {

    private OrderIdGenerator() {
        throw new AssertionError("유틸리티 클래스는 생성자로 생성할 수 없습니다."); // 일반적으로 private 생성자에 AssertionError를 많이 사용한다고 합니다.
    }

    private static final SecureRandom RANDOM = new SecureRandom(); // 보안을 위해 SecureRandom을 사용.
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
        for (int i = 0; i < SEGMENT_LENGTH; i++) {
            sb.append(CHAR_POOL.charAt(RANDOM.nextInt(CHAR_POOL.length())));
        }

        return sb.toString();
    }

    private static String nowYearMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }
}
