package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.common.exception.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookResponse;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookStockReduceRequest;
import com.nhnacademy.bookstoreorderapi.order.client.book.exception.InsufficientStockException;
import com.nhnacademy.bookstoreorderapi.order.client.book.service.BookService;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.UserResponse;
import com.nhnacademy.bookstoreorderapi.order.client.user.service.UserService;
import com.nhnacademy.bookstoreorderapi.order.common.exception.InvalidOrderStatusChangeException;
import com.nhnacademy.bookstoreorderapi.order.common.exception.MissingRequiredParameterException;
import com.nhnacademy.bookstoreorderapi.order.common.resolver.XUserIdResolver;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.*;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.BookNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.ReturnRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.StatusChangeRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.PurchaseVerificationResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.*;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import com.nhnacademy.bookstoreorderapi.order.service.OrderValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
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

    private final CustomOrderRepository customOrderRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CanceledOrderRepository canceledOrderRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final TaskScheduler taskScheduler;
    private final ReturnsRepository returnRepository;
    private final ApplicationContext applicationContext;

    private static final Duration DELIVERY_DELAY = Duration.ofSeconds(10);

    // 주문 생성
    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest, String xUserId) {
        // 사전 검증
        if (orderRequest == null) {
            throw new IllegalArgumentException("orderRequest는 null일 수 없습니다.");
        }
        Long userNo = getUserNo(xUserId);
        log.info("주문 생성 시작 - item's size: {}, userId: {}", orderRequest.orderItems().size(), userNo);

        // 도서 및 포장지 검증 및 조회
        List<OrderRequest.OrderItemRequest> itemRequests = orderRequest.orderItems();
        Map<Long, BookResponse> bookMap = orderValidationService.fetchAndValidateBooks(itemRequests);
        Map<Long, Wrapping> wrappingMap = orderValidationService.fetchAndValidateWrappings(itemRequests);

        //TODO: 포장비 포함시켜서 총 금액 계산해야됨.
        // 주문 생성
        Order order = Order.of(orderRequest, userNo);
        List<OrderItem> items = OrderItem.createItems(order, itemRequests, bookMap, wrappingMap);
        orderRepository.save(order);
        orderItemRepository.saveAll(items);

        reduceStock(itemRequests, bookMap);

        log.info("주문 완료: id={}, orderId={}, userNo={}, totalPrice={}, deliveryFee={}, address={}",
                order.getId(),
                order.getOrderId(),
                userNo,
                order.getTotalPrice(),
                order.getShippingInfo().getDeliveryFee(),
                order.getShippingInfo().getAddress());

        return OrderResponse.from(order);
    }

    // 회원 주문 전체 조회
    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> findAllByUserId(String xUserId, Pageable pageable) {
        Long userNo = getUserNo(xUserId);

        return customOrderRepository.findOrderSummary(userNo, pageable);
    }

    // 회원 주문 상세 조회
    @Override
    public OrderResponse findByOrderId(String orderId, String xUserId) {
        Long userNo = getUserNo(xUserId);
        Order order = orderRepository.findByOrderIdAndUserNo(orderId, userNo)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return OrderResponse.from(order);
    }

    // 주문 취소
    @Override
    @Transactional
    public void cancelOrder(String orderId, String reason) {

        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusChangeException("배송 전(PENDING) 상태만 취소 가능합니다.");
        }

        order.setStatus(OrderStatus.CANCELED);

        canceledOrderRepository.save(
                CanceledOrder.builder()
                        .orderId(order.getId())
                        .canceledAt(LocalDateTime.now())
                        .reason(reason)
                        .build());

        orderRepository.save(order);
    }

    @Transactional
    @Override
    public OrderResponse changeStatus(String orderId, StatusChangeRequest request, String xUserId) {
        // 사전 검증
        if (orderId == null || orderId.isBlank()) {
            log.debug("[Bad Request] 주문상태 변경 - orderId 누락: orderId={}", orderId);
            throw new MissingRequiredParameterException("주문번호는 필수입니다.");
        }
        if (request == null) {
            log.debug("[Bad Request] 주문상태 변경 요청 정보 누락: StatusChangeRequest=null");
            throw new MissingRequiredParameterException("주문상태 변경 요청 정보는 필수입니다.");
        }

        // (userNo, orderId) 조합으로 주문 조회
        Long createdBy = xUserIdResolver.resolveUserNo(xUserId);
        Order order = orderRepository.findByOrderIdAndUserNo(orderId, createdBy)
                .orElseThrow(() -> {
                    log.warn("주문을 찾을 수 없습니다: orderId={}, userNo={}", orderId, createdBy);
                    return new OrderNotFoundException(orderId);
                });

        log.debug("[사용자] 주문 상태 변경을 시작합니다: orderId={}, oldStatus={}, newStatus={}, createdBy={}",
                orderId, order.getStatus(), request.newStatus(), createdBy);

        // 주문 상태 변경
        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = request.newStatus();
        if (!oldStatus.canTransitionTo(newStatus)) {
            log.warn("[Bad Request] 주문 상태 변경 불가능: {} -> {}", oldStatus, newStatus);
            throw new InvalidOrderStatusChangeException(oldStatus.name() + " -> " + newStatus.name() + " 상태 변경이 불가능합니다.");
        } else if (!oldStatus.equals(OrderStatus.COMPLETED)) {
            log.warn("[Bad Request] 배송 완료된 상품만 주문 상태를 변경할 수 있습니다: oldStatus={}", oldStatus);
            throw new InvalidOrderStatusChangeException("사용자는 배송 완료된 상품만 주문 상태를 변경할 수 있습니다: oldStatus=" + oldStatus);
        }
        OrderStatusLog statusLog = new OrderStatusLog(oldStatus, newStatus, createdBy, request.memo(), order);

        order.setStatus(newStatus);
        statusLogRepository.save(statusLog);
        log.info("[사용자] 주문 상태가 변경되었습니다: orderId={}, oldStatus={}, newStatus={}, createdBy={}",
                statusLog.getOrder().getOrderId(), statusLog.getOldStatus(), statusLog.getNewStatus(), statusLog.getCreatedBy());

        // SHIPPING 상태로 변경 시 일정 시간 후 자동 배송 완료 스케줄링
        if (newStatus == OrderStatus.SHIPPING) {
            scheduleAutoDeliveryComplete(order.getOrderId());
        }

        return OrderResponse.from(order);
    }

//    // 주문 상태 변경
//    @Override
//    @Transactional
//    public StatusChangeResponseDto changeStatus(String orderId,
//                                                OrderStatus newStatus,
//                                                String memo,
//                                                String xUserId) {
//        if (!userService.getUserInfo(xUserId).isAuth()) {
//            throw new NotAdminException("관리자만 주문 상태를 변경할 수 있습니다");
//        }
//        Long changedBy = getUserNo(xUserId);
//
//        Order order = orderRepository.findByOrderId(orderId)
//                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));
//
//        OrderStatus oldStatus = order.getStatus();
//        if (!oldStatus.canTransitionTo(newStatus)) {
//            throw new InvalidOrderStatusChangeException(
//                    String.format("상태 전이 불가 : %s → %s", oldStatus, newStatus));
//        }
//
//        OrderStatusLog log = new OrderStatusLog(oldStatus, newStatus, changedBy, memo, order);
//        statusLogRepository.save(log);
//
//        order.setStatus(newStatus);
//        orderRepository.save(order);
//
//        if (newStatus == OrderStatus.SHIPPING) {
//            scheduleAutoDeliveryComplete(order.getId()); //TODO 주문: 자동으로 배송 완료 처리 되는 것도 로그 변경 이력을 남겨야하는데...
//        }
//
//        return StatusChangeResponseDto.createFrom(log);
//    }

    private void scheduleAutoDeliveryComplete(String orderId) {

        LocalDateTime runAt = LocalDateTime.now().plus(DELIVERY_DELAY);
        Date triggerTime = Date.from(runAt.atZone(ZoneId.systemDefault()).toInstant());

        taskScheduler.schedule(() -> {
            try {
                // Spring 프록시를 통해 @Transactional 메서드 호출
                OrderService orderService = applicationContext.getBean(OrderService.class);
                orderService.completeDelivery(orderId);
            } catch (Exception e) {
                log.error("자동 배송완료 처리 실패 for order {}", orderId, e);
            }
        }, triggerTime);
    }

    @Transactional
    @Override
    public void completeDelivery(String orderId) {

        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.SHIPPING) {
            return;
        }

        statusLogRepository.save(new OrderStatusLog(OrderStatus.SHIPPING, OrderStatus.COMPLETED, 99L, "배송 자동 완료", order));
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
    }

    // 반품 요청
    @Override
    @Transactional
    public int requestReturn(String orderId, ReturnRequest dto) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.RETURNED) {
            throw new InvalidOrderStatusChangeException("이미 반품 처리된 주문입니다.");
        }

        order.setStatus(OrderStatus.RETURNED);
        orderRepository.save(order);

        OrderReturn orderReturn = OrderReturn.createFrom(order, dto);
        returnRepository.save(orderReturn);

        return (int) (order.getTotalPrice() - OrderReturn.RETURNS_FEE);
    }

    //TODO: 변경 이력 조회는 관리자 말고 쓸 일이 없을듯? 사용자에게는 현재 주문 상태만 보여주면 됨.
//    // 상태 변경 이력 조회
//    @Override
//    @Transactional(readOnly = true)
//    public List<OrderStatusLogDto> getStatusLog(String orderId, String xUserId) {
//
//        Order order = orderRepository.findByOrderId(orderId)
//                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));
//
//        return statusLogRepository.findByOrderId(order.getId()).stream() //TODO 주문: 다른 엔티티에 주문ID가 orderid로 들어가 있어서 주문번호와 헷갈림.
//                .map(OrderStatusLogDto::createFrom)
//                .collect(Collectors.toList());
//    }

    @Transactional(readOnly = true)
    @Override
    public PurchaseVerificationResponse verifyPurchase(String xUserId, Long bookId) {
        Long userNo = getUserNo(xUserId);
        if (userNo == null || bookId == null) {
            throw new MissingRequiredParameterException("구매 검증에 필요한 정보(회원 정보 혹은 도서 정보)가 빠져있습니다.");
        }

        return customOrderRepository.findByUserNoAndBookId(userNo, bookId);
    }

    private void reduceStock(List<OrderRequest.OrderItemRequest> itemRequests,
                            Map<Long, BookResponse> bookMap) {
        Map<Long, Integer> quantityMap = itemRequests.stream()
                .collect(Collectors.groupingBy(
                        OrderRequest.OrderItemRequest::bookId,
                        Collectors.summingInt(OrderRequest.OrderItemRequest::quantity)
                ));

        List<BookStockReduceRequest> stockReduceRequests = new ArrayList<>(quantityMap.size());
        for (Map.Entry<Long, Integer> entry : quantityMap.entrySet()) {
            Long bookId = entry.getKey();
            Integer requestedQuantity = entry.getValue();
            BookResponse book = bookMap.get(bookId);

            int available = book.stock();
            validStock(book, bookId, available, requestedQuantity);
            stockReduceRequests.add(new BookStockReduceRequest(bookId, requestedQuantity));
        }
        bookService.stockUpdate(stockReduceRequests);
    }

    private void validStock(BookResponse book, Long bookId, Integer available, Integer requestedQuantity) {
        if (book == null) {
            throw new BookNotFoundException("책을 찾을 수 없습니다. id=" + bookId);
        }

        if (requestedQuantity > available) {
            throw new InsufficientStockException(
                    String.format("재고가 부족합니다. bookId=%d, 주문수량=%d, 재고=%d",
                            bookId, requestedQuantity, available)
            );
        }
    }

    private Long getUserNo(String xUserId) {
        if (xUserId == null || xUserId.isBlank()) {
            return null;
        }

        UserResponse userInfo = userService.getUserInfo(xUserId);
        return userInfo.userNo();
    }
}