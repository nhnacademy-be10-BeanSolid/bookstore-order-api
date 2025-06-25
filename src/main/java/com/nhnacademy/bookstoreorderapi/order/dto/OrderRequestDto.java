// src/main/java/com/nhnacademy/bookstoreorderapi/order/dto/OrderRequestDto.java
package com.nhnacademy.bookstoreorderapi.order.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OrderRequestDto {

    /* 기존 필드들 */
    private String              orderType;              // member | guest
    private String              userId;
    private Long                guestId;
    private String              orderAddress;
    private LocalDate           requestedDeliveryDate;
    private List<OrderItemDto>  items;

    /* ★ 새 필드 */
    private String  payMethod;
    private String  orderName;
    private int     payAmount;
}