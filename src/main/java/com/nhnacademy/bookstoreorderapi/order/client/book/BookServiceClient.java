package com.nhnacademy.bookstoreorderapi.order.client.book;

//import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookIdListRequest;
//import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookOrderListResponse;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookOrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "BOOK-API")
public interface BookServiceClient {

    //TODO 99: Book 도메인과 통신하기(책 여러 권 주문 요청에 대한 검증 목적) - ex: (GET /books/ids?ids=...)
    @GetMapping("/books/ids")
    ResponseEntity<List<BookOrderResponse>> getBookOrderResponse(@RequestParam("ids") List<Long> ids);

//    @GetMapping("/books")
//    ResponseEntity<BookOrderListResponse> getBookOrderList(BookIdListRequest bookIdListRequest);
}
