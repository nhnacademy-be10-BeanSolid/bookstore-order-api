package com.nhnacademy.bookstoreorderapi.order.client.book.dto;

public record BookStockReduceRequest(
    Long bookId,
    Integer stock
) {}
