package com.nhnacademy.bookstoreorderapi.order.service.impl;

import com.nhnacademy.bookstoreorderapi.common.exception.OrderNotFoundException;
import com.nhnacademy.bookstoreorderapi.order.client.user.exception.NotAdminException;
import com.nhnacademy.bookstoreorderapi.order.common.resolver.XUserIdResolver;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatusLog;
import com.nhnacademy.bookstoreorderapi.order.dto.request.StatusChangeRequest;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderStatusLogRepository;
import com.nhnacademy.bookstoreorderapi.order.service.OrderAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAdminServiceImpl implements OrderAdminService {

    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository orderStatusLogRepository;
    private final XUserIdResolver xUserIdResolver;


    @Transactional
    @Override
    public OrderResponse changeStatus(String orderId,
                                      StatusChangeRequest request,
                                      String xUserId) {
        if (!xUserIdResolver.isAdmin(xUserId)) {
            log.warn("[접근 제한] 관리자만 실행할 수 있는 기능입니다: xUserId={}", xUserId);
            throw new NotAdminException("관리자만 실행할 수 있는 기능입니다");
        }

        Long createdBy = xUserIdResolver.resolveUserNo(xUserId);
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("주문을 찾을 수 없습니다: orderId={}", orderId);
                    return new OrderNotFoundException(orderId);
                });

        log.debug("주문 상태 변경을 시작합니다: orderId={}, oldStatus={}, newStatus={}, createdBy={}",
                orderId, order.getStatus(), request.newStatus(), createdBy);

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = request.newStatus();
        OrderStatusLog statusLog = new OrderStatusLog(oldStatus, newStatus, createdBy, request.memo(), order);

        order.setStatus(newStatus);
        orderStatusLogRepository.save(statusLog);
        log.info("주문 상태가 변경되었습니다: orderId={}, oldStatus={}, newStatus={}, createdBy={}",
                statusLog.getOrder().getOrderId(), statusLog.getOldStatus(), statusLog.getNewStatus(), statusLog.getCreatedBy());

        return OrderResponse.from(order);
    }
}
