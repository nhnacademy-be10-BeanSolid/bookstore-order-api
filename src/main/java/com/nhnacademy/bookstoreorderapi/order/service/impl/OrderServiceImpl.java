package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.common.service.PointService;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.client.book.service.BookService;
import com.nhnacademy.bookstoreorderapi.order.common.resolver.XUserIdResolver;
import com.nhnacademy.bookstoreorderapi.order.domain.*;
import com.nhnacademy.bookstoreorderapi.order.dto.internal.OrderDetailInternal;
import com.nhnacademy.bookstoreorderapi.order.dto.request.CreateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderStatusRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.UpdateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.*;
import com.nhnacademy.bookstoreorderapi.order.exception.badrequest.InvalidOrderStatusChangeException;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.WrappingNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.exception.unauthorized.NotMemberException;
import com.nhnacademy.bookstoreorderapi.order.repository.*;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import com.nhnacademy.bookstoreorderapi.payment.dto.Response.PaymentResDto;
import com.nhnacademy.bookstoreorderapi.payment.service.PaymentService;
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

    public static final String ORDER_NOTFOUND_MESSAGE = "주문을 찾을 수 없습니다: orderNumber=";

    private final XUserIdResolver xUserIdResolver;

    private final BookService bookService;
    private final PointService pointService;
    private final PaymentService paymentService;

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
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOTFOUND_MESSAGE + orderNumber));
        List<OrderItem> orderItems = orderItemRepository.findAllByOrder(order);

        updateOrderItemsWithWrapping(orderItems, request.wrappingRequests());
        long totalPrice = calculateTotalPrice(orderItems);
        Integer shippingFee = calculateShippingFee(totalPrice, userNo);
        ShippingInfo shippingInfo = new ShippingInfo(request, shippingFee);
        
        order.setTotalPrice(totalPrice);
        order.setShippingInfo(shippingInfo);

        return OrderResponse.from(order);
    }


    private OrderDetailInternal getOrderDetail(String orderNumber, Long userNo) {
        Order order = orderRepository.findByOrderNumberAndUserNo(orderNumber, userNo)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOTFOUND_MESSAGE + orderNumber));
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
            return ShippingInfo.DEFAULT_SHIPPING_FEE;
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

    @Override
    @Transactional
    public OrderStatusResult changeOrderStatus(String orderNumber, OrderStatusRequest request, String xUserId) {
        Long userNo = xUserIdResolver.resolveUserNo(xUserId);
        if (userNo == null) {
            throw new NotMemberException("회원만 주문 상태 변경이 가능합니다.");
        }

        return switch (request.action()) {
            case CANCEL -> handleCancelOrder(orderNumber, request, userNo);
            case RETURN -> handleReturnOrder(orderNumber, request, userNo);
        };
    }

    private OrderStatusResult.CancelResult handleCancelOrder(String orderNumber, OrderStatusRequest request, Long userNo) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOTFOUND_MESSAGE + orderNumber));

        if (!order.getUserNo().equals(userNo)) {
            throw new NotMemberException("본인의 주문만 취소할 수 있습니다.");
        }

        if (order.getStatus() != OrderStatus.PENDING_PAY) {
            throw new InvalidOrderStatusChangeException("취소할 수 없는 주문 상태입니다: " + order.getStatus());
        }

        // 결제 취소 처리
        PaymentResDto paymentResult = paymentService.refundCardPaymentByOrderNumber(orderNumber, request.reason());

        // 포인트 반환 처리
        pointService.processPointRefund(order, paymentResult.getPayAmount());

        // 주문 상태 변경
        OrderStatusLog statusLog = new OrderStatusLog(
                order.getStatus(),
                OrderStatus.CANCELED,
                userNo,
                request.reason(),
                order
        );
        statusLogRepository.save(statusLog);
        order.setStatus(OrderStatus.CANCELED);

        return new OrderStatusResult.CancelResult(paymentResult);
    }

    private OrderStatusResult.ReturnResult handleReturnOrder(String orderNumber, OrderStatusRequest request, Long userNo) {
        boolean damaged = request.damaged() == true;
        
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOTFOUND_MESSAGE + orderNumber));

        if (!order.getUserNo().equals(userNo)) {
            throw new NotMemberException("본인의 주문만 반품할 수 있습니다.");
        }

        if (!statusLogRepository.canReturnOrder(order, damaged)) {
            throw new InvalidOrderStatusChangeException("반품 가능한 기간이 지났습니다.");
        }

        Long refundAmount = statusLogRepository.getCompletedOrderPaymentAmount(order, damaged)
                .orElseThrow(() -> new InvalidOrderStatusChangeException("반품 가능한 주문이 아닙니다."));

        pointService.processPointRefund(order, refundAmount);

        OrderReturn orderReturn = new OrderReturn(order, request.reason(), damaged);
        returnRepository.save(orderReturn);

        OrderStatusLog statusLog = new OrderStatusLog(
                order.getStatus(),
                OrderStatus.RETURNED,
                userNo,
                request.reason(),
                order
        );
        statusLogRepository.save(statusLog);
        order.setStatus(OrderStatus.RETURNED);

        OrderResponse orderResponse = OrderResponse.from(order);
        return new OrderStatusResult.ReturnResult(orderResponse);
    }
}
