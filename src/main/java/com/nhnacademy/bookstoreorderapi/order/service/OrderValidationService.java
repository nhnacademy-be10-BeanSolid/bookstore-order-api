package com.nhnacademy.bookstoreorderapi.order.service;

import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.client.book.service.BookService;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Wrapping;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.BookNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.WrappingNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import com.nhnacademy.bookstoreorderapi.order.repository.WrappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderValidationService {
    
    private final BookService bookService;
    private final WrappingRepository wrappingRepository;
    
    public Map<Long, BookResponse> fetchAndValidateBooks(List<OrderRequest.OrderItemRequest> itemRequests) {
        List<Long> bookIds = itemRequests.stream()
                .map(OrderRequest.OrderItemRequest::bookId)
                .toList();
        
        List<BookResponse> books = bookService.getBookOrderResponse(bookIds);
        
        if (books == null || books.size() != bookIds.size()) {
            throw new BookNotFoundException("일부 또는 전체 책을 찾을 수 없습니다: " + bookIds);
        }
        
        log.debug("{}권의 책을 가져왔습니다. ids={}", books.size(), bookIds);
        
        return books.stream()
                .collect(Collectors.toMap(BookResponse::id, Function.identity()));
    }
    
    @Transactional(readOnly = true)
    public Map<Long, Wrapping> fetchAndValidateWrappings(List<OrderRequest.OrderItemRequest> itemRequests) {
        List<Long> wrappingIds = itemRequests.stream()
                .map(OrderRequest.OrderItemRequest::wrappingId)
                .toList();
        
        List<Wrapping> wrappings = wrappingRepository.findAllById(wrappingIds);
        
        if (wrappings.size() != wrappingIds.size()) {
            throw new WrappingNotFoundException("일부 또는 전체 포장지를 찾을 수 없습니다: " + wrappingIds);
        }
        
        log.debug("{}개의 포장지를 가져왔습니다. ids={}", wrappings.size(), wrappingIds);
        
        return wrappings.stream()
                .collect(Collectors.toMap(Wrapping::getId, Function.identity()));
    }
}