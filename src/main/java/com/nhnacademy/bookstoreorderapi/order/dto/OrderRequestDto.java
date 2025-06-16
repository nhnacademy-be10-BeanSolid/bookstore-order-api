package com.nhnacademy.bookstoreorderapi.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 주문 생성‧수정 요청 DTO
 *
 * <pre>
 * ┌─────────┬─────────────────────────────────────────────┐
 * │ orderType│  "member"  → userId + orderAddress 필수     │
 * │          │  "guest"   → guestId 필수                  │
 * └─────────┴─────────────────────────────────────────────┘
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDto {

    /* 1. 주문 유형 – 반드시 member | guest */
    @NotBlank(message = "orderType 은 'member' 또는 'guest' 여야 합니다.")
    private String orderType;              // "member" | "guest"

    /* 2-A. 회원 주문 정보 */
    private String userId;                 // 회원 ID
    private String orderAddress;           // 배송지 (회원 필수)

    /* 2-B. 비회원 주문 정보 */
    private Long guestId;                  // 비회원 식별자

    /* 3. 배송 요청일(오늘 이후) – null 이면 오늘 */
    @FutureOrPresent(message = "deliveryDate 는 오늘 이후여야 합니다.")
    private LocalDate deliveryDate;

    /* 4. 주문 상품 목록 */
    @NotEmpty(message = "items 항목이 비어 있을 수 없습니다.")
    @Valid
    private List<OrderItemDto> items;

    /* ===== Bean-Validation 커스텀 검증 ===== */

    /** orderType = member → userId + orderAddress 모두 필요 */
    @AssertTrue(message = "orderType=member 일 때는 userId 와 orderAddress 가 모두 필요합니다.")
    private boolean isMemberValid() {
        return !isMember() ||
                (hasText(userId) && hasText(orderAddress));
    }

    /** orderType = guest → guestId 필요 */
    @AssertTrue(message = "orderType=guest 일 때는 guestId 가 필요합니다.")
    private boolean isGuestValid() {
        return !isGuest() || guestId != null;
    }

    /* ===== 내부 편의 메서드 ===== */

    private boolean isMember() { return "member".equalsIgnoreCase(orderType); }
    private boolean isGuest()  { return "guest".equalsIgnoreCase(orderType); }
    private boolean hasText(String s) { return s != null && !s.isBlank(); }
}