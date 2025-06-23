package com.nhnacademy.bookstoreorderapi.order.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 주문 요청 DTO에서 회원(userId)과 비회원(guestId) 식별자 중
 * 하나의 데이터만 존재하는지 검증하기 위한 커스텀 어노테이션입니다.
 *
 * <p>사용 예시</p>
 * <pre>
 *     &#64;ExactlyOneOfUserIdOrGuestId
 *     public class OrderRequestDto {
 *         private String userId;
 *         private Long guestId;
 *         // ...
 *     }
 * </pre>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrderRequestValidator.class)
public @interface ExactlyOneOfUserIdOrGuestId {

    String message() default "userId 또는 guestId 중 하나만 있어야 하고, 올바른 값이어야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
