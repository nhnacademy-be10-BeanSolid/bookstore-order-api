package com.nhnacademy.bookstoreorderapi.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDto {
    @NotBlank
    private String orderType; // member or guest

    private Long userId;
    private String guestName;
    private String guestPhone;

    // @NotNull 제거 → null 허용 (기본 날짜 처리)
    @FutureOrPresent(message = "배송일자는 오늘 또는 이후여야 합니다.")
    private LocalDate deliveryDate;

    @NotEmpty
    @Valid
    private List<OrderItemDto> items;

    @AssertTrue(message = "orderType = member 인 경우 userId 가 필요합니다.")
    public boolean isMemberValid() {
        return !"member".equalsIgnoreCase(orderType) || userId != null;
    }

    @AssertTrue(message = "orderType = guest 인 경우 guestName·guestPhone 이 필요합니다.")
    public boolean isGuestValid() {
        return !"guest".equalsIgnoreCase(orderType)
                || (guestName != null && !guestName.isBlank()
                 && guestPhone != null && !guestPhone.isBlank());
    }
}