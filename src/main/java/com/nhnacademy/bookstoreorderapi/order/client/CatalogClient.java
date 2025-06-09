package com.nhnacademy.bookstoreorderapi.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "catalog-service",
        url  = "${catalog.service.url}"
)
public interface CatalogClient {

    @GetMapping("/books/{bookId}")
    BookDto getBook(@PathVariable("bookId") Long bookId);

    // 응답 바인딩용 DTO
    public static class BookDto {
        private Long   id;
        private String title;
        private int    price;

        public Long   getId()    { return id; }
        public String getTitle() { return title; }
        public int    getPrice() { return price; }

        public void setId(Long id)         { this.id = id; }
        public void setTitle(String t)     { this.title = t; }
        public void setPrice(int price)    { this.price = price; }
    }
}