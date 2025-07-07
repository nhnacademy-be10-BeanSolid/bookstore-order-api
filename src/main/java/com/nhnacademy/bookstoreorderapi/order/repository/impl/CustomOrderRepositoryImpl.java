package com.nhnacademy.bookstoreorderapi.order.repository.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.QOrder;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.CustomOrderRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

@RequiredArgsConstructor
public class CustomOrderRepositoryImpl implements CustomOrderRepository {

    private final JPAQueryFactory factory;

    @Override
    public Page<OrderSummaryResponse> findOrderSummary(Long userNo, Pageable pageable) {
        QOrder order = QOrder.order;

        List<OrderSummaryResponse> content = factory
                .select(Projections.constructor(OrderSummaryResponse.class,
                        order.orderDate,
                        order.orderId,
                        order.shippingInfo.receiverName,
                        order.totalPrice))
                .from(order)
                .where(order.userNo.eq(userNo))
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
}
