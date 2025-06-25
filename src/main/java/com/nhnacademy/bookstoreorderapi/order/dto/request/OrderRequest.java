package com.nhnacademy.bookstoreorderapi.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotBlank(message = "배송지는 빈 문자열 혹은 공백이면 안됩니다")
    private String address;

    @Future(message = "배송 요청일은 내일 이후여야 합니다.")
    private LocalDate requestedDeliveryDate;

    // 주문 상품 목록
    @NotEmpty(message = "items 항목이 비어 있을 수 없습니다.")
    @Valid // @Valid가 있어야 OrderItemDto에 대한 값 검증이 이루어짐.(컬렉션 타입에 대한 재귀적 검증)
    private List<OrderItemRequest> items;
}
