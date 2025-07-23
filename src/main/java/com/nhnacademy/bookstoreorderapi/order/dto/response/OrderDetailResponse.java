package com.nhnacademy.bookstoreorderapi.order.dto.response;

import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.domain.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Schema(description = "주문 상세 응답 DTO")
public class OrderDetailResponse {

    @Schema(description = "주문 날짜", example = "2024-01-01")
    private LocalDate orderDate;
    
    @Schema(description = "주문 번호", example = "202507-abcdef-123456")
    private String orderNumber;
    
    @Schema(description = "주문 상태", example = "PENDING")
    private String status;
    
    @Schema(description = "총 주문 금액", example = "50000")
    private Long totalAmount;
    
    @Schema(description = "주문 상품 정보 목록")
    private List<ItemInfo> itemInfos;
    
    @Schema(description = "받는 사람 이름", example = "홍길동")
    private String receiverName;
    
    @Schema(description = "받는 사람 전화번호", example = "010-1234-5678")
    private String receiverPhoneNumber;
    
    @Schema(description = "배송지 주소", example = "서울시 강남구 테헤란로 123")
    private String address;
    
    @Schema(description = "배송 요청 날짜", example = "2024-01-03")
    private LocalDate requestedDeliveryDate;
    
    @Schema(description = "배송비", example = "3000")
    private Integer deliveryFee;

    @Getter
    @AllArgsConstructor
    @Schema(description = "주문 상품 상세 정보")
    public static class ItemInfo {

        @Schema(description = "도서 제목", example = "Spring Boot 완벽 가이드")
        private String title;
        
        @Schema(description = "주문 수량", example = "2")
        private Integer quantity;
        
        @Schema(description = "도서 가격", example = "25000")
        private Long bookPrice;
        
        @Schema(description = "포장지 이름", example = "고급 포장지")
        private String wrappingName;
        
        @Schema(description = "포장비", example = "1000")
        private Integer wrappingPrice;
    }

    public static OrderDetailResponse of(Order order, List<OrderItem> items, List<BookResponse> books) {
        Map<Long, BookResponse> bookMap = books.stream()
                .collect(Collectors.toMap(BookResponse::id, Function.identity()));
        
        List<ItemInfo> itemInfos = items.stream()
                .map(item -> {
                    BookResponse book = bookMap.get(item.getBookId());
                    String title = book != null ? book.title() : "Unknown";
                    
                    String wrappingName = item.getWrapping() != null ? item.getWrapping().getName() : null;
                    Integer wrappingPrice = item.getWrapping() != null ? item.getWrapping().getPrice() : null;
                    
                    return new ItemInfo(
                            title,
                            item.getQuantity(),
                            (long) item.getUnitPrice(),
                            wrappingName,
                            wrappingPrice
                    );
                })
                .toList();
        
        return new OrderDetailResponse(
                order.getOrderDate(),
                order.getOrderNumber(),
                order.getStatus().name(),
                order.getTotalPrice(),
                itemInfos,
                order.getShippingInfo().getReceiverName(),
                order.getShippingInfo().getReceiverPhoneNumber(),
                order.getShippingInfo().getAddress(),
                order.getShippingInfo().getRequestedDeliveryDate(),
                order.getShippingInfo().getShippingFee()
        );
    }
}
