package com.nhnacademy.bookstoreorderapi.order.client.book.service;

import com.nhnacademy.bookstoreorderapi.order.client.book.BookServiceClient;
import com.nhnacademy.bookstoreorderapi.order.client.ExternalServiceException;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookOrderResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookOrderService {

    private final BookServiceClient bookServiceClient;

    @CircuitBreaker(name = "bookService", fallbackMethod = "fallbackBookOrders")
    public List<BookOrderResponse> getBookOrderResponse(List<Long> ids) {
        return bookServiceClient.getBookOrderResponse(ids).getBody();
    }

    public List<BookOrderResponse> fallbackBookOrders(List<Long> ids, Throwable t) {
        log.warn("Fallback - book IDs: {}", ids);
        throw new ExternalServiceException("BookServiceClient Error", t);
    }
}
