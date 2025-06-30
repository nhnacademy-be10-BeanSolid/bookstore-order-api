package com.nhnacademy.bookstoreorderapi.order.client.book;

//import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookIdListRequest;
//import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookOrderListResponse;

import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookOrderResponse;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookStockReduceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "BOOK-API")
public interface BookServiceClient {

    @GetMapping("/books/ids")
    ResponseEntity<List<BookOrderResponse>> getBookOrderResponse(@RequestParam("ids") List<Long> ids);

    @PatchMapping("/book-reduce")
    ResponseEntity<Void> stockUpdate(@RequestBody List<BookStockReduceRequest> requests);

//    @GetMapping("/books")
//    ResponseEntity<BookOrderListResponse> getBookOrderList(BookIdListRequest bookIdListRequest);
}
