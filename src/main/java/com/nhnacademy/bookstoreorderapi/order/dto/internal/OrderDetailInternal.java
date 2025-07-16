package com.nhnacademy.bookstoreorderapi.order.dto.internal;

import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.domain.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.OrderItem;

import java.util.List;

public record OrderDetailInternal(
        Order order,
        List<OrderItem> orderItems,
        List<BookResponse> books
) {}
