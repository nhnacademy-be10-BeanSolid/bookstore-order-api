package com.nhnacademy.bookstoreorderapi.order.domain;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 랜덤 문자열 생성 클래스.
 */
public class OrderIdGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGIT = "0123456789";
    private static final String SPECIAL_CHARACTER = "-_";
    private static final String ALL = UPPER + LOWER + DIGIT + SPECIAL_CHARACTER;

    /**
     * 길이 64인 랜덤 문자열을 생성하여 {@link com.nhnacademy.bookstoreorderapi.order.domain.entity.Order Order}의 orderId로 설정한다.
     *
     * @return 길이 64인 랜덤 문자열
     */
    public static String generate() {

        // 필수 포함 문자 4개
        List<Character> chars = new ArrayList<>(List.of(
                randomFrom(UPPER), randomFrom(LOWER), randomFrom(DIGIT), randomFrom(SPECIAL_CHARACTER)
        ));

        // 나머지 60개 문자는 전체에서 랜덤 생성
        for (int i = 0; i < 60; i++) {
            chars.add(randomFrom(ALL));
        }

        Collections.shuffle(chars, random);
        StringBuilder sb = new StringBuilder(64);
        chars.forEach(sb::append);

        return sb.toString();
    }

    private static char randomFrom(String s) {

        return s.charAt(random.nextInt(s.length()));
    }
}
