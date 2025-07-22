package com.nhnacademy.bookstoreorderapi.order.dto.response;

import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.domain.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Schema(description = "주문 생성 응답 DTO")
public class CreateOrderResponse {

    @Schema(description = "주문 번호", example = "202507-abcdef-123456")
    private String orderNumber;
    
    @Schema(description = "주문 상품 목록")
    private List<CreateOrderItemResponse> orderItems;

    public static CreateOrderResponse of(Order order, List<OrderItem> orderItems, List<BookResponse> books) {
        Map<Long, BookResponse> bookMap = books.stream()
                .collect(Collectors.toMap(BookResponse::id, Function.identity()));
        
        List<CreateOrderItemResponse> itemResponses = orderItems.stream()
                .map(item -> CreateOrderItemResponse.of(item, bookMap.get(item.getBookId())))
                .toList();
        
        return new CreateOrderResponse(order.getOrderNumber(), itemResponses);
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "주문 상품 응답 정보")
    public static class CreateOrderItemResponse {

        @Schema(description = "도서 ID", example = "1")
        private Long bookId;
        
        @Schema(description = "도서 제목", example = "Spring Boot 완벽 가이드")
        private String bookTitle;
        
        @Schema(description = "단가", example = "25000")
        private Integer unitPrice;
        
        @Schema(description = "수량", example = "2")
        private Integer quantity;
        
        @Schema(description = "포장 가능 여부", example = "true")
        private Boolean wrappable;
        
        public static CreateOrderItemResponse of(OrderItem orderItem, BookResponse book) {
            return new CreateOrderItemResponse(
                    orderItem.getBookId(),
                    orderItem.getBookTitle(),
                    orderItem.getUnitPrice(),
                    orderItem.getQuantity(),
                    book.wrappable()
            );
        }
    }
}
