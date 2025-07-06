package com.nhnacademy.bookstoreorderapi.order.client.book.service;

import com.nhnacademy.bookstoreorderapi.order.client.book.BookServiceClient;
import com.nhnacademy.bookstoreorderapi.order.client.ExternalServiceException;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookStockReduceRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookServiceClient bookServiceClient;

    @CircuitBreaker(name = "book-service", fallbackMethod = "fallbackGetBookOrderResponse")
    public List<BookResponse> getBookOrderResponse(List<Long> ids) {
        log.debug("책 정보를 가져옵니다 - ids: {}", ids);
        return bookServiceClient.getBookOrderResponse(ids);
    }

    @CircuitBreaker(name = "book-service", fallbackMethod = "fallbackStockUpdate")
    public void stockUpdate(List<BookStockReduceRequest> requests) {
        log.debug("책 재고를 차감합니다");
        bookServiceClient.stockUpdate(requests);
    }

    public List<BookResponse> fallbackGetBookOrderResponse(List<Long> ids, Throwable t) {
        log.warn("BookService getBookOrderResponse fallback 실행 - bookIds: {}", ids);
        throw new ExternalServiceException("BookServiceClient Error", t);
    }

    public void fallbackStockUpdate(List<BookStockReduceRequest> requests, Throwable t) {
        log.warn("BookService stockUpdate fallback 실행 - bookIds: {}", requests.stream().map(BookStockReduceRequest::bookId).toList());
        throw new ExternalServiceException("BookServiceClient Error", t);
    }
}
