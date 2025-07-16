package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.order.common.resolver.XUserIdResolver;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatusLog;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAdminServiceImpl implements OrderAdminService {

    private final XUserIdResolver xUserIdResolver;
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;

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

        return OrderResponse.from(order);
    }

    private void validateAdminAccess(String xUserId) {
        if (!xUserIdResolver.isAdmin(xUserId)) {
            log.warn("관리자가 아닌 사용자가 접근했습니다: xUserId={}", xUserId);
            throw new NotAdminException("관리자 권한이 필요합니다.");
        }
    }
}
