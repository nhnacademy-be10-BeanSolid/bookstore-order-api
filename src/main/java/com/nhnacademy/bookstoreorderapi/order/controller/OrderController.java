// src/main/java/com/nhnacademy/bookstoreorderapi/order/controller/OrderController.java
package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderResponseDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderStatusLogDto;
import com.nhnacademy.bookstoreorderapi.order.dto.StatusChangeResponseDto;
import com.nhnacademy.bookstoreorderapi.order.dto.ReturnRequestDto;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    /**
     * 1) 주문 생성
     */
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto req) {
        long newId = orderService.createOrder(req);
        Order created = orderRepository.findById(newId)
                .orElseThrow(() -> new IllegalStateException("생성된 주문을 조회할 수 없습니다: " + newId));
        OrderResponseDto dto = OrderResponseDto.createFrom(created);
        return ResponseEntity
                .created(URI.create("/api/v1/orders/" + newId))
                .body(dto);
    }

    /**
     * 2) 사용자별 주문 조회
     */
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> listByUser(@RequestParam String userId) {
        List<OrderResponseDto> list = orderService.listByUser(userId);
        return ResponseEntity.ok(list);
    }

    /**
     * 3) 주문 취소
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable long orderId,
            @RequestParam String reason) {

        orderService.cancelOrder(orderId, reason);
        return ResponseEntity.noContent().build();
    }

    /**
     * 4) 상태 변경
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<StatusChangeResponseDto> changeStatus(
            @PathVariable long orderId,
            @RequestParam OrderStatus newStatus,
            @RequestParam Long changedBy,
            @RequestParam String memo) {

        StatusChangeResponseDto res = orderService.changeStatus(
                orderId,
                newStatus,
                changedBy,
                memo
        );
        return ResponseEntity.ok(res);
    }

    /**
     * 5) 반품 요청
     */
    @PostMapping("/{orderId}/return")
    public ResponseEntity<Map<String,Integer>> requestReturn(
            @PathVariable long orderId,
            @RequestBody ReturnRequestDto req) {

        int refund = orderService.requestReturn(orderId, req);
        return ResponseEntity.ok(Map.of("refundAmount", refund));
    }

    /**
     * 6) 상태 로그 조회
     */
    @GetMapping("/{orderId}/logs")
    public ResponseEntity<List<OrderStatusLogDto>> getStatusLog(
            @PathVariable long orderId) {

        List<OrderStatusLogDto> logs = orderService.getStatusLog(orderId);
        return ResponseEntity.ok(logs);
    }
}