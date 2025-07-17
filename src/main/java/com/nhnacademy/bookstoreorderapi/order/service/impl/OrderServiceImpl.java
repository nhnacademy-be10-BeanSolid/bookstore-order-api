package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.common.service.PointService;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.client.book.service.BookService;
import com.nhnacademy.bookstoreorderapi.order.common.resolver.XUserIdResolver;
import com.nhnacademy.bookstoreorderapi.order.domain.*;
import com.nhnacademy.bookstoreorderapi.order.dto.internal.OrderDetailInternal;
import com.nhnacademy.bookstoreorderapi.order.dto.request.CreateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.ReturnsRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.UpdateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.CreateOrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderDetailResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.exception.badrequest.InvalidOrderStatusChangeException;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.WrappingNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.exception.unauthorized.NotMemberException;
import com.nhnacademy.bookstoreorderapi.order.repository.*;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final XUserIdResolver xUserIdResolver;

    private final BookService bookService;
    private final PointService pointService;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WrappingRepository wrappingRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final ReturnsRepository returnRepository;

    // CREATE (생성)
    // 주문 생성
    @Transactional
    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request, String xUserId) {
        Long userNo = xUserIdResolver.resolveUserNo(xUserId);

        List<CreateOrderRequest.CreateOrderItemRequest> mergedItems = mergeQuantitiesByBookId(request.createItemRequests());
        log.debug("주문 생성 시작: item's size={}, userNo={}", mergedItems.size(), userNo);

        List<BookResponse> books = fetchBookInformation(mergedItems);
        Map<Long, Integer> quantityMap = createQuantityMap(mergedItems);

        Order order = new Order(userNo);
        List<OrderItem> orderItems = createOrderItems(books, quantityMap, order);

        Order saved = orderRepository.save(order);
        List<OrderItem> savedItems = orderItemRepository.saveAll(orderItems);
        log.debug("주문 생성 완료: userNo={}, orderNumber={}, item's size={}", saved.getUserNo(), saved.getOrderNumber(), savedItems.size());

        return CreateOrderResponse.of(saved, savedItems, books);
    }

    // READ (조회)
    // 주문서 작성 페이지
    @Transactional(readOnly = true)
    @Override
    public CreateOrderResponse getUnfinishedOrder(String orderNumber, String xUserId) {
        Long userNo = xUserIdResolver.resolveUserNo(xUserId);
        OrderDetailInternal unfinished = getOrderDetail(orderNumber, userNo);
        return CreateOrderResponse.of(unfinished.order(), unfinished.orderItems(), unfinished.books());
    }

    // 회원 주문 전체 조회
    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> findAllByUserId(String xUserId, Pageable pageable) {
        Long userNo = xUserIdResolver.resolveUserNo(xUserId);
        return orderRepository.findOrderSummary(userNo, pageable);
    }

    // 주문 상세 조회
    @Transactional(readOnly = true)
    @Override
    public OrderDetailResponse findByOrderNumber(String orderNumber, String xUserId) {
        Long userNo = xUserIdResolver.resolveUserNo(xUserId);
        OrderDetailInternal data = getOrderDetail(orderNumber, userNo);
        return OrderDetailResponse.of(data.order(), data.orderItems(), data.books());
    }

    // UPDATE (수정)
    @Transactional
    @Override
    public OrderResponse updateOrder(String orderNumber, UpdateOrderRequest request, String xUserId) {
        Long userNo = xUserIdResolver.resolveUserNo(xUserId);
        Order order = orderRepository.findByOrderNumberAndUserNo(orderNumber, userNo)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다: orderNumber=" + orderNumber));
        List<OrderItem> orderItems = orderItemRepository.findAllByOrder(order);

        updateOrderItemsWithWrapping(orderItems, request.wrappingRequests());
        long totalPrice = calculateTotalPrice(orderItems);
        Integer shippingFee = calculateShippingFee(totalPrice, userNo);
        ShippingInfo shippingInfo = new ShippingInfo(request, shippingFee);
        
        order.setTotalPrice(totalPrice);
        order.setShippingInfo(shippingInfo);

        return OrderResponse.from(order);
    }

    // 반품 요청
    @Transactional
    @Override
    public OrderResponse changeStatusToReturned(String orderNumber, ReturnsRequest request, String xUserId) {
        Long userNo = xUserIdResolver.resolveUserNo(xUserId);
        if (userNo == null) {
            log.warn("[경고] 비회원이 반품 기능에 접근함");
            throw new NotMemberException("회원이 아닙니다");
        }
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다: orderNumber=" + orderNumber));

        if (!statusLogRepository.canReturnOrder(order, request.damaged())) {
            throw new InvalidOrderStatusChangeException("반품 가능한 기간이 지났습니다.");
        }

        Long refundAmount = statusLogRepository.getCompletedOrderPaymentAmount(order, request.damaged())
                .orElseThrow(() -> new InvalidOrderStatusChangeException("반품 가능한 주문이 아닙니다."));

        pointService.processPointRefund(order, refundAmount);

        OrderReturn orderReturn = new OrderReturn(order, request.reason(), request.damaged());
        returnRepository.save(orderReturn);

        OrderStatusLog statusLog = new OrderStatusLog(order.getStatus(), OrderStatus.RETURNED, userNo, request.reason(), order);
        order.setStatus(OrderStatus.RETURNED);
        statusLogRepository.save(statusLog);

        return OrderResponse.from(order);
    }

    private OrderDetailInternal getOrderDetail(String orderNumber, Long userNo) {
        Order order = orderRepository.findByOrderNumberAndUserNo(orderNumber, userNo)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다: orderNumber=" + orderNumber));
        List<OrderItem> orderItems = orderItemRepository.findAllByOrder(order);

        List<Long> bookIds = orderItems.stream()
                .map(OrderItem::getBookId)
                .toList();
        List<BookResponse> books = bookService.getBookOrderResponse(bookIds);
        return new OrderDetailInternal(order, orderItems, books);
    }

    private List<OrderItem> createOrderItems(List<BookResponse> books, Map<Long, Integer> quantityMap, Order order) {
        return books.stream()
                .map(book -> new OrderItem(book.id(), book.title(), book.salePrice(), quantityMap.get(book.id()), order))
                .toList();
    }

    private Map<Long, Integer> createQuantityMap(List<CreateOrderRequest.CreateOrderItemRequest> itemRequests) {
        return itemRequests.stream()
                .collect(Collectors.toMap(
                        CreateOrderRequest.CreateOrderItemRequest::bookId,
                        CreateOrderRequest.CreateOrderItemRequest::quantity
                ));
    }

    private List<BookResponse> fetchBookInformation(List<CreateOrderRequest.CreateOrderItemRequest> itemRequests) {
        List<Long> bookIds = itemRequests.stream()
                .mapToLong(CreateOrderRequest.CreateOrderItemRequest::bookId)
                .boxed()
                .toList();

        List<BookResponse> books = bookService.getBookOrderResponse(bookIds);
        log.debug("책 조회 완료: {}권, bookIds={}", books.size(), bookIds);

        return books;
    }

    private void updateOrderItemsWithWrapping(List<OrderItem> orderItems, List<UpdateOrderRequest.WrappingRequest> wrappingRequests) {
        Map<Long, Long> wrappingMap = wrappingRequests.stream()
                .filter(req -> req.wrappingId() != null)
                .collect(Collectors.toMap(
                        UpdateOrderRequest.WrappingRequest::bookId,
                        UpdateOrderRequest.WrappingRequest::wrappingId
                ));

        for (OrderItem orderItem : orderItems) {
            Long wrappingId = wrappingMap.get(orderItem.getBookId());
            if (wrappingId != null) {
                Wrapping wrapping = wrappingRepository.findById(wrappingId)
                        .orElseThrow(() -> new WrappingNotFoundException("포장지를 찾을 수 없습니다: wrappingId=" + wrappingId));
                orderItem.setWrapping(wrapping);
            }
        }
    }

    private long calculateTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                .mapToLong(orderItem -> {
                    long itemPrice = (long) orderItem.getUnitPrice() * orderItem.getQuantity();
                    if (orderItem.getWrapping() != null) {
                        itemPrice += orderItem.getWrapping().getPrice();
                    }
                    return itemPrice;
                })
                .sum();
    }

    private Integer calculateShippingFee(long totalPrice, Long userNo) {
        if (userNo == null) {
            return (int) ShippingInfo.DEFAULT_SHIPPING_FEE;
        }
        return totalPrice >= ShippingInfo.FREE_SHIPPING_THRESHOLD ? 0 : ShippingInfo.DEFAULT_SHIPPING_FEE;
    }

    private List<CreateOrderRequest.CreateOrderItemRequest> mergeQuantitiesByBookId(List<CreateOrderRequest.CreateOrderItemRequest> itemRequests) {
        return itemRequests.stream()
                .collect(Collectors.groupingBy(
                        CreateOrderRequest.CreateOrderItemRequest::bookId,
                        Collectors.summingInt(CreateOrderRequest.CreateOrderItemRequest::quantity)
                ))
                .entrySet().stream()
                .map(entry -> new CreateOrderRequest.CreateOrderItemRequest(entry.getKey(), entry.getValue()))
                .toList();
    }
}