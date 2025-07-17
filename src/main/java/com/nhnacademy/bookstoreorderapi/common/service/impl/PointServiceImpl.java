package com.nhnacademy.bookstoreorderapi.common.service.impl;

import com.nhnacademy.bookstoreorderapi.common.service.PointService;
import com.nhnacademy.bookstoreorderapi.order.client.user.UserServiceClient;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.response.ResponsePointType;
import com.nhnacademy.bookstoreorderapi.order.domain.Order;
import com.nhnacademy.bookstoreorderapi.payment.domain.entity.Payment;
import com.nhnacademy.bookstoreorderapi.payment.exception.NonMemberPointUsageAttemptException;
import com.nhnacademy.bookstoreorderapi.payment.exception.NotEnoughPointException;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.request.OrderPointPlusProcessRequest;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.request.OrderPointMinusProcessRequest;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.request.PointType;
import com.nhnacademy.bookstoreorderapi.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {
    private final UserServiceClient userServiceClient;
    private final PaymentRepository paymentRepository;

    @Override
    public void processEarnedPoints(Order order, Payment payment) {
        Long userNo = order.getUserNo();
        if (userNo != null) {
            ResponsePointType earningRateResponse = userServiceClient.getEarningRateByUserNo(userNo);
            int earningRate = earningRateResponse.getEarningRate();
            int earnedPoint = (int) (payment.getPayAmount() * (earningRate / 100.0));

            if (earnedPoint > 0) {
                OrderPointPlusProcessRequest pointPlusRequest = new OrderPointPlusProcessRequest(
                        order.getId(),
                        earnedPoint,
                        PointType.ORDER
                );
                userServiceClient.orderPointPlusProcess(userNo, pointPlusRequest);
            }
        }
    }

    @Override
    public void processUsedPoints(Order order, Payment payment) {
        Long userNo = order.getUserNo();
        if (payment.getUsedPoint() > 0) {
            OrderPointMinusProcessRequest pointMinusRequest = new OrderPointMinusProcessRequest(
                    order.getId(),
                    payment.getUsedPoint(),
                    PointType.ORDER
            );
            userServiceClient.orderPointMinusProcess(userNo, pointMinusRequest);
        }
    }

    @Override
    public void validatePointUsage(Long userNo, Integer usedPoint) {
        if (usedPoint > 0) {
            if (userNo == null) { // 비회원이 포인트를 사용하려는 경우
                throw new NonMemberPointUsageAttemptException("비회원은 포인트를 사용할 수 없습니다.");
            }
            // 회원인 경우에만 포인트 검사
            Integer currentUserPoint = userServiceClient.getUserPointByUserNo(userNo);
            if (currentUserPoint == null || currentUserPoint < usedPoint) {
                throw new NotEnoughPointException("사용하려는 포인트가 부족합니다.");
            }
        }
    }

    @Override
    public void processPointRefund(Order order, Long refundAmount) {
        Long userNo = order.getUserNo();
        if (userNo == null) { // 비회원은 포인트 반환 대상이 아님
            return;
        }

        // 결제 금액에 대한 포인트 반환 로직
        OrderPointPlusProcessRequest pointPlusRequest = new OrderPointPlusProcessRequest(
                order.getId(),
                refundAmount.intValue(),
                PointType.RETURN
        );
        userServiceClient.orderPointPlusProcess(userNo, pointPlusRequest);

        // 사용된 포인트 반환 로직
        paymentRepository.findByOrder(order).ifPresent(payment -> {
            if (payment.getUsedPoint() > 0) {
                OrderPointPlusProcessRequest usedPointPlusRequest = new OrderPointPlusProcessRequest(
                        order.getId(),
                        payment.getUsedPoint(),
                        PointType.RETURN
                );
                userServiceClient.orderPointPlusProcess(userNo, usedPointPlusRequest);
            }
        });
    }
}
