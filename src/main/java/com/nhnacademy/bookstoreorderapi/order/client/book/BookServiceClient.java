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

    @GetMapping("/books/ids")
    ResponseEntity<List<BookOrderResponse>> getBookOrderResponse(@RequestParam("ids") List<Long> ids);

    //TODO 도서: 재고 차감 API 들어갈 예정.

//    @GetMapping("/books")
//    ResponseEntity<BookOrderListResponse> getBookOrderList(BookIdListRequest bookIdListRequest);
}
