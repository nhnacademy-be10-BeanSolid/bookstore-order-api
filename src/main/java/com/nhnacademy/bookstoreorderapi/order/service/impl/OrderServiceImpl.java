package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.client.book.service.BookService;
import com.nhnacademy.bookstoreorderapi.order.client.user.service.UserService;
import com.nhnacademy.bookstoreorderapi.order.common.resolver.XUserIdResolver;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.*;
import com.nhnacademy.bookstoreorderapi.order.dto.internal.OrderData;
import com.nhnacademy.bookstoreorderapi.order.dto.request.CreateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.ReturnsRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.UpdateOrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.CreateOrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderDetailResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.exception.badrequest.InvalidOrderStatusChangeException;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.WrappingNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.repository.*;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import com.nhnacademy.bookstoreorderapi.order.service.OrderValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final XUserIdResolver xUserIdResolver;

    private final OrderValidationService orderValidationService;

    private final BookService bookService;
    private final UserService userService;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WrappingRepository wrappingRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final TaskScheduler taskScheduler;
    private final ReturnsRepository returnRepository;
    private final ApplicationContext applicationContext;

    private static final Duration DELIVERY_DELAY = Duration.ofSeconds(10);

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

    @Transactional(readOnly = true)
    @Override
    public CreateOrderResponse getUnfinishedOrder(String orderNumber, String xUserId) {
        Long userNo = xUserIdResolver.resolveUserNo(xUserId);
        OrderData unfinished = getOrderDetail(orderNumber, userNo);
        return CreateOrderResponse.of(unfinished.order(), unfinished.orderItems(), unfinished.books());
    }

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
    public OrderResponse changeStatusToReturned(String orderNumber, ReturnsRequest request, Long userNo) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다: orderNumber=" + orderNumber));

        if (!statusLogRepository.canReturnOrder(order, request.damaged())) {
            throw new InvalidOrderStatusChangeException("반품 가능한 기간이 지났습니다.");
        }

        Long refundAmount = statusLogRepository.getCompletedOrderPaymentAmount(order, request.damaged())
                .orElseThrow(() -> new InvalidOrderStatusChangeException("반품 가능한 주문이 아닙니다."));
        userService.plusPoint(userNo, refundAmount.intValue());

        OrderReturn orderReturn = new OrderReturn(order, request.reason(), request.damaged());
        returnRepository.save(orderReturn);

        OrderStatusLog statusLog = new OrderStatusLog(order.getStatus(), OrderStatus.RETURNED, userNo, request.reason(), order);
        order.setStatus(OrderStatus.RETURNED);
        statusLogRepository.save(statusLog);

        return OrderResponse.from(order);
    }

    // 주문 상세 조회
    @Transactional(readOnly = true)
    @Override
    public OrderDetailResponse findByOrderNumber(String orderNumber, String xUserId) {
        Long userNo = xUserIdResolver.resolveUserNo(xUserId);
        OrderData data = getOrderDetail(orderNumber, userNo);
        return OrderDetailResponse.of(data.order(), data.orderItems(), data.books());
    }

    private OrderData getOrderDetail(String orderNumber, Long userNo) {
        Order order = orderRepository.findByOrderNumberAndUserNo(orderNumber, userNo)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다: orderNumber=" + orderNumber));
        List<OrderItem> orderItems = orderItemRepository.findAllByOrder(order);

        List<Long> bookIds = orderItems.stream()
                .map(OrderItem::getBookId)
                .toList();
        List<BookResponse> books = bookService.getBookOrderResponse(bookIds);
        return new OrderData(order, orderItems, books);
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
//
//    // 회원 주문 전체 조회
//    @Override
//    @Transactional(readOnly = true)
//    public Page<OrderSummaryResponse> findAllByUserId(String xUserId, Pageable pageable) {
//        Long userNo = getUserNo(xUserId);
//
//        return customOrderRepository.findOrderSummary(userNo, pageable);
//    }
//

//
//    private void scheduleAutoDeliveryComplete(String orderId) {
//
//        LocalDateTime runAt = LocalDateTime.now().plus(DELIVERY_DELAY);
//        Date triggerTime = Date.from(runAt.atZone(ZoneId.systemDefault()).toInstant());
//
//        taskScheduler.schedule(() -> {
//            try {
//                // Spring 프록시를 통해 @Transactional 메서드 호출
//                OrderService orderService = applicationContext.getBean(OrderService.class);
//                orderService.completeDelivery(orderId);
//            } catch (Exception e) {
//                log.error("자동 배송완료 처리 실패 for order {}", orderId, e);
//            }
//        }, triggerTime);
//    }
//
//    @Transactional
//    @Override
//    public void completeDelivery(String orderId) {
//
//        Order order = orderRepository.findByOrderId(orderId)
//                .orElseThrow(() -> new OrderNotFoundException(orderId));
//
//        if (order.getStatus() != OrderStatus.SHIPPING) {
//            return;
//        }
//
//        statusLogRepository.save(new OrderStatusLog(OrderStatus.SHIPPING, OrderStatus.COMPLETED, 99L, "배송 자동 완료", order));
//        order.setStatus(OrderStatus.COMPLETED);
//        orderRepository.save(order);
//    }
//
//    @Transactional(readOnly = true)
//    @Override
//    public PurchaseVerificationResponse verifyPurchase(String xUserId, Long bookId) {
//        Long userNo = getUserNo(xUserId);
//        if (userNo == null || bookId == null) {
//            throw new MissingRequiredParameterException("구매 검증에 필요한 정보(회원 정보 혹은 도서 정보)가 빠져있습니다.");
//        }
//
//        return customOrderRepository.findByUserNoAndBookId(userNo, bookId);
//    }
//
//    private void reduceStock(List<OrderRequest.OrderItemRequest> itemRequests,
//                            Map<Long, BookResponse> bookMap) {
//        Map<Long, Integer> quantityMap = itemRequests.stream()
//                .collect(Collectors.groupingBy(
//                        OrderRequest.OrderItemRequest::bookId,
//                        Collectors.summingInt(OrderRequest.OrderItemRequest::quantity)
//                ));
//
//        List<BookStockReduceRequest> stockReduceRequests = new ArrayList<>(quantityMap.size());
//        for (Map.Entry<Long, Integer> entry : quantityMap.entrySet()) {
//            Long bookId = entry.getKey();
//            Integer requestedQuantity = entry.getValue();
//            BookResponse book = bookMap.get(bookId);
//
//            int available = book.stock();
//            validStock(book, bookId, available, requestedQuantity);
//            stockReduceRequests.add(new BookStockReduceRequest(bookId, requestedQuantity));
//        }
//        bookService.stockUpdate(stockReduceRequests);
//    }
//
//    private void validStock(BookResponse book, Long bookId, Integer available, Integer requestedQuantity) {
//        if (book == null) {
//            throw new BookNotFoundException("책을 찾을 수 없습니다. id=" + bookId);
//        }
//
//        if (requestedQuantity > available) {
//            throw new InsufficientStockException(
//                    String.format("재고가 부족합니다. bookId=%d, 주문수량=%d, 재고=%d",
//                            bookId, requestedQuantity, available)
//            );
//        }
//    }
//
//    private Long getUserNo(String xUserId) {
//        if (xUserId == null || xUserId.isBlank()) {
//            return null;
//        }
//
//        UserResponse userInfo = userService.getUserInfo(xUserId);
//        return userInfo.userNo();
//    }
}