package com.nhnacademy.bookstoreorderapi.order.client.book.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updateAt,
        String status,
        int stock
) {}
