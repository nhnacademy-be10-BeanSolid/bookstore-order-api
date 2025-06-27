package com.nhnacademy.bookstoreorderapi.order.client.book.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record BookOrderResponse (
        Long id,
        String title,
        String description,
        String toc,
        String author,
        String publisher,
        LocalDate publishedDate,
        String isbn,
        int originalPrice,
        int salePrice,
        boolean wrappable,
        LocalDateTime createAt,
        LocalDateTime updateAt,
        String status,
        int stock
) {}
