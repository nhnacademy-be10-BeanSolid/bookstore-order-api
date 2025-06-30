package com.nhnacademy.bookstoreorderapi.order.client.book.service;

import com.nhnacademy.bookstoreorderapi.order.client.book.BookServiceClient;
import com.nhnacademy.bookstoreorderapi.order.client.ExternalServiceException;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookOrderResponse;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookStockReduceRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookOrderService {

    private final BookServiceClient bookServiceClient;

    @CircuitBreaker(name = "book-service", fallbackMethod = "fallbackGetBook")
    public List<BookOrderResponse> getBookOrderResponse(List<Long> ids) {
        return bookServiceClient.getBookOrderResponse(ids).getBody();
    }

    @CircuitBreaker(name = "book-service", fallbackMethod = "fallbackStockUpdate")
    public void stockUpdate(List<BookStockReduceRequest> requests) {
        bookServiceClient.stockUpdate(requests);
    }

    public List<BookOrderResponse> fallbackGetBook(List<Long> ids, Throwable t) {
        log.warn("Fallback_getBook - book IDs: {}", ids);
        throw new ExternalServiceException("BookServiceClient Error", t);
    }

    public void fallbackStockUpdate(List<BookStockReduceRequest> requests, Throwable t) {
        log.warn("Fallback_stockUpdate - book IDs: {}", requests.stream().map(BookStockReduceRequest::bookId).toList());
        throw new ExternalServiceException("BookServiceClient Error", t);
    }
}
