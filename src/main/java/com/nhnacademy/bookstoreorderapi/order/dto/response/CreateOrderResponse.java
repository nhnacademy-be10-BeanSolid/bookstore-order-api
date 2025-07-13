package com.nhnacademy.bookstoreorderapi.order.dto.response;

import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class CreateOrderResponse {

    private String orderNumber;
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
    public static class CreateOrderItemResponse {

        private Long bookId;
        private String bookTitle;
        private Integer unitPrice;
        private Integer quantity;
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
