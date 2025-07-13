package com.nhnacademy.bookstoreorderapi.order.dto.response;

import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class OrderDetailResponse {

    private LocalDate orderDate;
    private String orderId;
    private String status;
    private Long totalAmount;
    private List<ItemInfo> itemInfos;
    private String receiverName;
    private String receiverPhoneNumber;
    private String address;
    private LocalDate requestedDeliveryDate;
    private Integer deliveryFee;

    @Getter
    @AllArgsConstructor
    public static class ItemInfo {

        private String title;
        private Integer quantity;
        private Long bookPrice;
        private String wrappingName;
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
