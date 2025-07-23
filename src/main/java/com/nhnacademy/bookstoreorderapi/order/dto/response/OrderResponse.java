package com.nhnacademy.bookstoreorderapi.order.dto.response;

import com.nhnacademy.bookstoreorderapi.order.domain.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Schema(description = "주문 응답 DTO")
public class OrderResponse {

    @Schema(description = "주문 ID", example = "1")
    private Long orderId;
    
    @Schema(description = "주문 번호", example = "202507-abcdef-123456")
    private String orderNumber;
    
    @Schema(description = "사용자 번호", example = "12345")
    private Long userNo;
    
    @Schema(description = "주문 상태", example = "PENDING")
    private String status;
    
    @Schema(description = "주문 날짜", example = "2024-01-01")
    private LocalDate orderDate;
    
    @Schema(description = "총 주문 금액", example = "50000")
    private Long totalPrice;
    
    @Schema(description = "받는 사람 이름", example = "홍길동")
    private String receiverName;
    
    @Schema(description = "받는 사람 전화번호", example = "010-1234-5678")
    private String receiverPhoneNumber;
    
    @Schema(description = "배송지 주소", example = "서울시 강남구 테헤란로 123")
    private String address;
    
    @Schema(description = "배송 요청 날짜", example = "2024-01-03")
    private LocalDate requestedDeliveryDate;
    
    @Schema(description = "배송비", example = "3000")
    private Integer shippingFee;

    public static OrderResponse from(Order o) {
        String statusName = o.getStatus() == null ? null : o.getStatus().name();

        return new OrderResponse(
                o.getId(),
                o.getOrderNumber(),
                o.getUserNo(),
                statusName,
                o.getOrderDate(),
                o.getTotalPrice(),
                o.getShippingInfo().getReceiverName(),
                o.getShippingInfo().getReceiverPhoneNumber(),
                o.getShippingInfo().getAddress(),
                o.getShippingInfo().getRequestedDeliveryDate(),
                o.getShippingInfo().getShippingFee()
        );
    }
}