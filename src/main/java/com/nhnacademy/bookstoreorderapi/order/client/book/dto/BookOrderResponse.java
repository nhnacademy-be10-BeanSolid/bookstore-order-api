package com.nhnacademy.bookstoreorderapi.order.client.book.dto;

//TODO 도서: 필요한 필드 다시 생각해보고 최종 결정하기
public record BookOrderResponse(
        Long id,
        int unitPrice,
        int stock,
        String title
) {}
// 1. book_id 필요함.
// 2. 책 제목 필요함.
// 3. 가격 필요함.
// 4. 재고? 필요할 듯.