package com.nhnacademy.bookstoreorderapi.order.repository.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.QOrder;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.QOrderItem;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.PurchaseVerificationResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.UserOrderAmountResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.CustomOrderRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomOrderRepositoryImpl implements CustomOrderRepository {

    private final JPAQueryFactory factory;

    @Override
    public Page<OrderSummaryResponse> findOrderSummary(Long userNo, Pageable pageable) {
        QOrder order = QOrder.order;

        List<OrderSummaryResponse> content = factory
                .select(Projections.constructor(OrderSummaryResponse.class,
                        order.orderDate,
                        order.orderNumber,
                        order.shippingInfo.receiverName,
                        order.totalPrice))
                .from(order)
                .where(order.userNo.eq(userNo)
                        .and(order.status.isNotNull()))
                .orderBy(order.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = factory
                .select(order.count())
                .from(order)
                .where(order.userNo.eq(userNo));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<UserOrderAmountResponse> findOrderAmountGroupByUserLastThreeMonths() {
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;

        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);

        return factory
                .select(Projections.constructor(UserOrderAmountResponse.class,
                        order.userNo,
                        orderItem.unitPrice.longValue()
                                .multiply(orderItem.quantity.longValue())
                                .sum()
                                .as("pureOrderAmount")))
                .from(orderItem)
                .join(orderItem.order, order)
                .where(order.orderDate.goe(threeMonthsAgo)
                        .and(order.status.in(OrderStatus.PENDING, OrderStatus.SHIPPING, OrderStatus.COMPLETED)))
                .groupBy(order.userNo)
                .fetch();
    }

    @Override
    public PurchaseVerificationResponse findByUserNoAndBookId(Long userNo, Long bookId) {
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;

        boolean exists = factory
                .selectOne()
                .from(orderItem)
                .join(orderItem.order, order)
                .where(order.userNo.eq(userNo)
                        .and(orderItem.bookId.eq(bookId))
                        .and(order.status.in(OrderStatus.PENDING, OrderStatus.SHIPPING, OrderStatus.COMPLETED)))
                .fetchFirst() != null;

        return new PurchaseVerificationResponse(userNo, bookId, exists);
    }
}
