package com.nhnacademy.bookstoreorderapi.order.validation;

import com.nhnacademy.bookstoreorderapi.order.dto.OrderRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OrderRequestValidator implements ConstraintValidator<ExactlyOneOfUserIdOrGuestId, OrderRequestDto> {

    @Override
    public boolean isValid(OrderRequestDto dto, ConstraintValidatorContext ctx) {

        boolean hasUser = dto.getUserId() != null && !dto.getUserId().isBlank();
        boolean hasGuest = dto.getGuestId() != null;

        // 둘 다 존재하지 않거나, 둘 다 존재하면 실패
        if (hasUser == hasGuest) {
            ctx.disableDefaultConstraintViolation(); // @ExactlyOneOfUserIdOrGuestId 정의된 기본 메시지를 무시
            ctx.buildConstraintViolationWithTemplate("userId 또는 guestId 중 하나만 존재해야 합니다.")
                    .addConstraintViolation(); // 메시지 생성 후 Context에 메시지 추가

            return false;
        }

        return true;
    }
}
