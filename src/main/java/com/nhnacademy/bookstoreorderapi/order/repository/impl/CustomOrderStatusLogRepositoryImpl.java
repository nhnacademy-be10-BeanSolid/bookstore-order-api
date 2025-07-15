package com.nhnacademy.bookstoreorderapi.order.repository.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.entity.QOrderStatusLog;
import com.nhnacademy.bookstoreorderapi.order.repository.CustomOrderStatusLogRepository;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.QPayment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CustomOrderStatusLogRepositoryImpl implements CustomOrderStatusLogRepository {

    private final JPAQueryFactory factory;

    @Override
    public boolean canReturnOrder(Order order, boolean damaged) {
        QOrderStatusLog orderStatusLog = QOrderStatusLog.orderStatusLog;

        LocalDate cutOffDate = damaged ?
                LocalDate.now().minusDays(30) :
                LocalDate.now().minusDays(10);

        return factory
                .selectFrom(orderStatusLog)
                .where(orderStatusLog.order.eq(order)
                        .and(orderStatusLog.newStatus.eq(OrderStatus.SHIPPING))
                        .and(orderStatusLog.createdAt.after(cutOffDate.atStartOfDay())))
                .fetchFirst() != null;
    }

    @Override
    public Optional<Long> getCompletedOrderPaymentAmount(Order order) {
        QPayment payment = QPayment.payment;
        QOrderStatusLog orderStatusLog = QOrderStatusLog.orderStatusLog;

        Long payAmount = factory
                .select(payment.payAmount)
                .from(payment)
                .join(orderStatusLog).on(orderStatusLog.order.eq(payment.order))
                .where(payment.order.eq(order)
                        .and(orderStatusLog.newStatus.eq(OrderStatus.COMPLETED)))
                .fetchFirst();

        return Optional.ofNullable(payAmount);
    }
}
