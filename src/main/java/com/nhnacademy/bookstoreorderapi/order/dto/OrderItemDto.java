package com.nhnacademy.bookstoreorderapi.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItemDto {

    private Long     bookId;
    private int      quantity;
    private Boolean  giftWrapped;   // true/false
    private Long     wrappingId;    // 포장 옵션 ID (nullable)

    /* ★ 추가: 단가 */
    @JsonProperty("unitPrice")      // JSON 키와 매핑
    private Integer unitPrice;
}