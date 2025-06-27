package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.client.book.BookServiceClient;
import com.nhnacademy.bookstoreorderapi.order.client.book.dto.BookOrderResponse;
import com.nhnacademy.bookstoreorderapi.order.client.user.UserServiceClient;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.UserOrderResponse;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.*;
import com.nhnacademy.bookstoreorderapi.order.domain.exception.*;
import com.nhnacademy.bookstoreorderapi.order.dto.*;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderItemRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.request.OrderRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.*;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final BookServiceClient bookServiceClient;
    private final UserServiceClient userServiceClient;

    private final OrderRepository orderRepository;
    private final WrappingRepository wrappingRepository;
    private final CanceledOrderRepository canceledOrderRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final TaskScheduler taskScheduler;
    private final ReturnsRepository returnRepository;
    private final OrderItemRepository orderItemRepository;

    private static final Duration DELIVERY_DELAY = Duration.ofSeconds(5);

    // 주문 생성
    @Override
    @Transactional
    public void createOrder(OrderRequest orderRequest, String xUserId) { //TODO 주문: 도서 재고 확인해서 주문량보다 적으면 오류 발생시키기

        // parameters validation
        ResponseEntity<UserOrderResponse> userInfo = userServiceClient.getUserInfo(xUserId);
        Long userNo = userInfo != null ? validFeignClientResponse(userInfo).userNo() : null; // 회원 도메인은 PK를 userNo로 명명함.
        validParameters(orderRequest);
        log.info("주문 생성 시작: item's size={}, userId={}", orderRequest.items().size(), userNo);

        Order order = Order.of(orderRequest, userNo);

        List<OrderItemRequest> itemRequests = orderRequest.items();
        Map<Long, BookOrderResponse> bookMap = fetchBooks(itemRequests);
        Map<Long, Wrapping> wrappingMap = fetchWrappings(itemRequests);

        List<OrderItem> items = buildOrderItems(order, itemRequests, bookMap, wrappingMap);

        long totalPrice = calculateTotal(items);
        order.setTotalPrice(totalPrice);
        ShippingInfo shippingInfo = ShippingInfo.of(orderRequest, determineFee(totalPrice, userNo));
        order.setShippingInfo(shippingInfo);

        orderRepository.save(order);
        log.info("주문 완료: id={}, orderId={}, userNo={}, totalPrice={}, deliveryFee={}, address={}",
                order.getId(),
                order.getOrderId(),
                userNo,
                order.getTotalPrice(),
                shippingInfo.deliveryFee(),
                shippingInfo.address());
    }

    // 회원 주문 전체 조회
    @Override
    @Transactional
    public List<OrderSummaryResponse> findAllByUserId(String xUserId) {

        //TODO 회원: xUserId 값으로 userId(내부 PK) 받아오는 API로 변환하기
        Long userId = Long.parseLong(xUserId); // 임시

        List<Order> orders = orderRepository.findAllByUserId(userId);
        if (orders.isEmpty()) {
            throw new OrderNotFoundException("주문을 찾을 수 없습니다.");
        }

        return getOrderSummaryResponses(orders);
    }

    // 회원 주문 상세 조회
    @Override
    public OrderResponse findByOrderId(String orderId, String xUserId) {

        //TODO 회원: xUserId 값으로 userId(내부 PK) 받아오는 API로 변환하기
        Long userId = Long.parseLong(xUserId);

        Order order = orderRepository.findByOrderIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다. 주문번호: " + orderId));

        return OrderResponse.from(order);
    }

    private List<OrderSummaryResponse> getOrderSummaryResponses(List<Order> orders) {

        List<OrderSummaryResponse> orderList = new ArrayList<>();
        for (Order o : orders) {
            Long bookId = o.getItems().getFirst().getBookId();
            String bookTitle = bookServiceClient.getBookOrderResponse(List.of(bookId)).getBody().getFirst().title();

            OrderSummaryResponse orderSummaryResponse = OrderSummaryResponse.of(o, bookTitle);
            orderList.add(orderSummaryResponse);
        }
        return orderList;
    }

    private int determineFee(long totalPrice, Long userId) {
        final int THRESHOLD = 30_000;
        final int FEE = 5_000;

        return totalPrice >= THRESHOLD && Objects.nonNull(userId) ? 0 : FEE;
    }

    private long calculateTotal(List<OrderItem> items) {
        return items.stream().mapToLong(i -> (long) i.getUnitPrice() * i.getQuantity()).sum();
    }

    private List<OrderItem> buildOrderItems(Order order,
                                            List<OrderItemRequest> itemRequests,
                                            Map<Long, BookOrderResponse> bookMap,
                                            Map<Long, Wrapping> wrappingMap)
    {
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest req : itemRequests) {
            BookOrderResponse book = Objects.requireNonNull(bookMap.get(req.bookId()),
                    "book을 찾을 수 없습니다. 찾을 수 없는 id: " + req.bookId());
            Wrapping wrapping = Objects.requireNonNull(wrappingMap.get(req.wrappingId()),
                    "wrapping을 찾을 수 없습니다. 찾을 수 없는 id: " + req.wrappingId());
            OrderItem item = OrderItem.of(book, req.quantity());

            order.addItem(item);
            wrapping.addItem(item);
            items.add(item);
        }

        //TODO 주문: 영속성 전파(cascade)가 설정이 되어 있다면 saveAll을 생략할 수 있다고 함. 되어 있다면 지우기.
        wrappingRepository.saveAll(wrappingMap.values());
        orderItemRepository.saveAll(items);
        log.debug("wrapping & orderItem 저장 완료, item's size={}", items.size());

        return items;
    }

    private Map<Long, Wrapping> fetchWrappings(List<OrderItemRequest> itemRequests) {

        List<Long> ids = itemRequests.stream().map(OrderItemRequest::wrappingId).toList();
        List<Wrapping> wrappings = wrappingRepository.findAllById(ids);
        if (wrappings.size() != new HashSet<>(ids).size())
            throw new WrappingNotFoundException("wrapping의 개수가 일치하지 않습니다: " + ids);
        log.debug("{}개의 포장지를 가져옵니다. ids={}", wrappings.size(), ids);
        return wrappings.stream().collect(Collectors.toMap(Wrapping::getId, Function.identity()));
    }

    private Map<Long, BookOrderResponse> fetchBooks(List<OrderItemRequest> itemRequests) {

        List<Long> ids = itemRequests.stream().map(OrderItemRequest::bookId).toList();
        List<BookOrderResponse> books = bookServiceClient.getBookOrderResponse(ids).getBody();
        if (books == null || books.isEmpty())
            throw new BookNotFoundException("일치하는 책이 아무 것도 없습니다: " + ids);
        log.debug("{}권의 책을 가져옵니다. ids={}", books.size(), ids);
        return books.stream().collect(Collectors.toMap(BookOrderResponse::id, Function.identity()));
    }

    /*───────────────────────────────────────────────────────
     * 3. 주문 취소
     *──────────────────────────────────────────────────────*/
    @Override
    @Transactional
    public void cancelOrder(Long orderId, String reason) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusChangeException("배송 전(PENDING) 상태만 취소 가능합니다.");
        }

        order.setStatus(OrderStatus.CANCELED);

        canceledOrderRepository.save(
                CanceledOrder.builder()
                        .orderId(orderId)
                        .canceledAt(LocalDateTime.now())
                        .reason(reason)
                        .build());

        orderRepository.save(order);
    }

    /*───────────────────────────────────────────────────────
     * 4. 주문 상태 변경
     *──────────────────────────────────────────────────────*/
    @Override
    @Transactional
    public StatusChangeResponseDto changeStatus(
            Long orderId,
            OrderStatus newStatus,
            Long changedBy,
            String memo
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));

        OrderStatus oldStatus = order.getStatus();
        if (!oldStatus.canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusChangeException(
                    String.format("상태 전이 불가 : %s → %s", oldStatus, newStatus));
        }

        OrderStatusLog log = OrderStatusLog.createFrom(orderId, oldStatus, newStatus, changedBy, memo);
        statusLogRepository.save(log);

        order.setStatus(newStatus);
        orderRepository.save(order);

        if (newStatus == OrderStatus.SHIPPING) {
            scheduleAutoDeliveryComplete(orderId);
        }

        return StatusChangeResponseDto.createFrom(log);
    }

    private void scheduleAutoDeliveryComplete(Long orderId) {

        LocalDateTime runAt = LocalDateTime.now().plus(DELIVERY_DELAY);
        Date triggerTime = Date.from(runAt.atZone(ZoneId.systemDefault()).toInstant());

        taskScheduler.schedule(() -> {
            try {
                completeDelivery(orderId);
            } catch (Exception e) {
                log.error("자동 배송완료 처리 실패 for order {}", orderId, e);
            }
        }, triggerTime);
    }

    @Transactional
    public void completeDelivery(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다."));

        if (order.getStatus() != OrderStatus.SHIPPING) {
            return;
        }

        statusLogRepository.save(OrderStatusLog.createFrom(orderId, OrderStatus.SHIPPING, OrderStatus.COMPLETED, 99L, "배송 자동 완료"));
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
    }

    /*───────────────────────────────────────────────────────
     * 5. 반품 요청
     *──────────────────────────────────────────────────────*/
    @Override
    @Transactional
    public int requestReturn(Long orderId, ReturnRequestDto dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));

        if (order.getStatus() == OrderStatus.RETURNED) {
            throw new InvalidOrderStatusChangeException("이미 반품 처리된 주문입니다.");
        }

        order.setStatus(OrderStatus.RETURNED);
        orderRepository.save(order);

        OrderReturn orderReturn = OrderReturn.createFrom(order, dto);
        returnRepository.save(orderReturn);

        return (int) (order.getTotalPrice() - OrderReturn.RETURNS_FEE);
    }

    /*───────────────────────────────────────────────────────
     * 6. 상태 변경 이력 조회
     *──────────────────────────────────────────────────────*/
    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusLogDto> getStatusLog(Long orderId) {

        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("주문을 찾을 수 없습니다.");
        }

        return statusLogRepository.findByOrderId(orderId).stream()
                .map(OrderStatusLogDto::createFrom)
                .collect(Collectors.toList());
    }


    private <T> T validFeignClientResponse(ResponseEntity<T> resp) {

        if (resp == null) {
            return null;
        } else if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new RuntimeException();
        }

        return resp.getBody();
    }

    private void validParameters(Object... parameters) {

        for (int i = 0; i < parameters.length; i++) {

            Object param = parameters[i];
            if (param == null) {
                throw new IllegalArgumentException(String.format("parameter is null: index[%d]", i));
            }
            if (param instanceof String && ((String) param).isBlank()) {
                throw new IllegalArgumentException(String.format("String parameter is blank: index[%d]", i));
            }
        }
    }
}