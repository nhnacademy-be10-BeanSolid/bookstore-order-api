package com.nhnacademy.bookstoreorderapi.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
public class CatalogController {

    public record BookDto(Long id, String title, int price) {}

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBook(@PathVariable Long id) {
        // 스텁 데이터: id가 42면 "스프링 입문", 10000원
        if (id == 42L) {
            return ResponseEntity.ok(new BookDto(42L, "스프링 입문", 10000));
        }
        // 그 외는 미존재
        return ResponseEntity.notFound().build();
    }
}