package com.nhnacademy.bookstoreorderapi.order.repository.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.Order;
import com.nhnacademy.bookstoreorderapi.order.domain.OrderReturn;
import com.nhnacademy.bookstoreorderapi.order.domain.OrderStatus;
import com.nhnacademy.bookstoreorderapi.order.domain.QOrderStatusLog;
import com.nhnacademy.bookstoreorderapi.order.repository.CustomOrderStatusLogRepository;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.QPayment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
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

        log.debug("createdAt: {}", orderStatusLog.createdAt.after(cutOffDate.atStartOfDay()).isTrue());

        boolean b = factory
                .selectFrom(orderStatusLog)
                .where(orderStatusLog.order.eq(order)
                        .and(orderStatusLog.newStatus.eq(OrderStatus.SHIPPING))
                        .and(orderStatusLog.createdAt.after(cutOffDate.atStartOfDay())))
                .fetchFirst() != null;

        return b;
    }

    @Override
    public Optional<Long> getCompletedOrderPaymentAmount(Order order, boolean damaged) {
        QPayment payment = QPayment.payment;
        QOrderStatusLog orderStatusLog = QOrderStatusLog.orderStatusLog;

        if (!order.getStatus().equals(OrderStatus.COMPLETED)) {
            return Optional.empty();
        }

        Long payAmount = factory
                .select(payment.payAmount)
                .from(payment)
                .join(orderStatusLog).on(orderStatusLog.order.eq(payment.order))
                .where(payment.order.eq(order)
                        .and(orderStatusLog.newStatus.eq(OrderStatus.COMPLETED))
                        .and(orderStatusLog.createdAt.eq(
                                factory.select(orderStatusLog.createdAt.max())
                                        .from(orderStatusLog)
                                        .where(orderStatusLog.order.eq(order))
                        )))
                .fetchFirst();
        if (payAmount == null) {
            return Optional.empty();
        }

        long refundAmount = damaged ? payAmount : payAmount - OrderReturn.RETURNS_FEE;
        return Optional.of(refundAmount);
    }
}
