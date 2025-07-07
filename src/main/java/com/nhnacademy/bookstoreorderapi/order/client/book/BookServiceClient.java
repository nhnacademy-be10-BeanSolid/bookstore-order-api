package com.nhnacademy.bookstoreorderapi.order.client.book;

import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookStockReduceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "BOOK-API")
public interface BookServiceClient {

    @GetMapping("/books/ids")
    List<BookResponse> getBookOrderResponse(@RequestParam("ids") List<Long> ids);

    @PatchMapping("/book-reduce")
    void stockUpdate(@RequestBody List<BookStockReduceRequest> requests);
}
