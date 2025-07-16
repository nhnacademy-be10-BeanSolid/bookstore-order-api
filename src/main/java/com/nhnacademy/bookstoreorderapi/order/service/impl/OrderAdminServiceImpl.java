package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.common.resolver.XUserIdResolver;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatusLog;
import com.nhnacademy.bookstoreorderapi.order.dto.internal.ScheduledOrderCompletion;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.exception.forbidden.NotAdminException;
import com.nhnacademy.bookstoreorderapi.order.exception.notfound.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderStatusLogRepository;
import com.nhnacademy.bookstoreorderapi.order.service.OrderAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAdminServiceImpl implements OrderAdminService {

    private final XUserIdResolver xUserIdResolver;
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    
    private final Map<String, ScheduledOrderCompletion> scheduledCompletions = new ConcurrentHashMap<>();

    @Transactional
    @Override
    public Page<OrderSummaryResponse> getAllOrders(Pageable pageable, String xUserId) {
        validateAdminAccess(xUserId);
        return orderRepository.findAllOrderSummary(pageable);
    }

    @Transactional
    @Override
    public OrderResponse changeStatusToShipping(String orderNumber, String xUserId) {
        validateAdminAccess(xUserId);

        Long createdBy = xUserIdResolver.resolveUserNo(xUserId);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> {
                    log.warn("주문을 찾을 수 없습니다: orderNumber={}", orderNumber);
                    return new OrderNotFoundException("주문을 찾을 수 없습니다: orderNumber=" + orderNumber);
                });

        log.debug("[관리자] 주문 상태 변경을 시작합니다: orderNumber={}, oldStatus={}, newStatus={}, createdBy={}",
                orderNumber, order.getStatus(), OrderStatus.SHIPPING, createdBy);

        // 주문 상태 변경
        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = OrderStatus.SHIPPING;
        OrderStatusLog statusLog = new OrderStatusLog(oldStatus, newStatus, createdBy, null, order);

        order.setStatus(newStatus);
        orderStatusLogRepository.save(statusLog);
        log.info("[관리자] 주문 상태가 변경되었습니다: orderNumber={}, oldStatus={}, newStatus={}, createdBy={}",
                statusLog.getOrder().getOrderNumber(), statusLog.getOldStatus(), statusLog.getNewStatus(), statusLog.getCreatedBy());

        scheduleOrderCompletion(orderNumber, createdBy);

        return OrderResponse.from(order);
    }

    private void scheduleOrderCompletion(String orderNumber, Long createdBy) {
        LocalDateTime completionTime = LocalDateTime.now().plusSeconds(10);
        scheduledCompletions.put(orderNumber, new ScheduledOrderCompletion(orderNumber, createdBy, completionTime));
        log.info("[스케줄링] 주문 자동 완료가 예약되었습니다: orderNumber={}, completionTime={}", orderNumber, completionTime);
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void processScheduledCompletions() {
        LocalDateTime now = LocalDateTime.now();
        
        scheduledCompletions.entrySet().removeIf(entry -> {
            ScheduledOrderCompletion completion = entry.getValue();
            if (completion.completionTime().isBefore(now) || completion.completionTime().isEqual(now)) {
                try {
                    completeOrder(completion.orderNumber(), completion.createdBy());
                    return true;
                } catch (Exception e) {
                    log.error("[자동완료] 주문 완료 처리 실패: orderNumber={}, error={}", 
                             completion.orderNumber(), e.getMessage(), e);
                    return true;
                }
            }
            return false;
        });
    }

    private void completeOrder(String orderNumber, Long createdBy) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> {
                    log.warn("[자동완료] 주문을 찾을 수 없습니다: orderNumber={}", orderNumber);
                    return new OrderNotFoundException("주문을 찾을 수 없습니다: orderNumber=" + orderNumber);
                });

        if (order.getStatus() == OrderStatus.SHIPPING) {
            OrderStatus oldStatus = order.getStatus();
            OrderStatus newStatus = OrderStatus.COMPLETED;
            OrderStatusLog statusLog = new OrderStatusLog(oldStatus, newStatus, createdBy, null, order);

            order.setStatus(newStatus);
            orderStatusLogRepository.save(statusLog);
            
            log.info("[자동완료] 주문이 자동으로 완료되었습니다: orderNumber={}, oldStatus={}, newStatus={}, createdBy={}",
                    orderNumber, oldStatus, newStatus, createdBy);
        } else {
            log.warn("[자동완료] 주문 상태가 SHIPPING이 아니어서 완료처리를 건너뜁니다: orderNumber={}, currentStatus={}", 
                    orderNumber, order.getStatus());
        }
    }

    private void validateAdminAccess(String xUserId) {
        if (!xUserIdResolver.isAdmin(xUserId)) {
            log.warn("관리자가 아닌 사용자가 접근했습니다: xUserId={}", xUserId);
            throw new NotAdminException("관리자 권한이 필요합니다.");
        }
    }

}
