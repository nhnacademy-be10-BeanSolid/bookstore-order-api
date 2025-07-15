package com.nhnacademy.bookstoreorderapi.order.dto.internal;

import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderItem;

import java.util.List;

public record OrderData(
        Order order,
        List<OrderItem> orderItems,
        List<BookResponse> books
) {}
