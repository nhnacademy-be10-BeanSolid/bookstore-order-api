package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.dto.CancelOrderRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderResponseDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderStatusLogDto;
import com.nhnacademy.bookstoreorderapi.order.dto.StatusChangeRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.StatusChangeResponseDto;
import com.nhnacademy.bookstoreorderapi.order.dto.SuccessResponseDto;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;


    @GetMapping
    public List<OrderResponseDto> listMyOrders(@RequestParam String userId) {
        return orderService.listByUser(userId);
    }

    @PostMapping//수정 - 각 API가 자신의 구체적인 DTO만 책임지도록!
    public OrderResponseDto createOrder(@Valid @RequestBody OrderRequestDto req) {
        validateOrderType(req); //회원, 비회원 추가 체크
        return orderService.createOrder(req); //통과하면 호출해서 실제 주문 등록
    }

    @PatchMapping("/{orderId}/status")
    public StatusChangeResponseDto changeStatus(
            @PathVariable Long orderId, //url경로에서 orderId 뽑아서 바디에 newStatus, changeBy, memo 담은 statuschangeRequestDto받고
            //서비스에 넘겨서 주문 상태를 바꾼 뒤, 변경 결과를 돌려줌?
            @Valid @RequestBody StatusChangeRequestDto dto
    ) {
        return orderService.changeStatus(
                orderId,
                dto.getNewStatus(),
                dto.getChangedBy(),
                dto.getMemo()
        );
    }

    @PostMapping("/{orderId}/cancel")
    public SuccessResponseDto cancelOrder(
            @PathVariable Long orderId, // orderId만으로 취소 처리
            @RequestBody(required = false) CancelOrderRequestDto dto
    ) {
        //취소 사유가 있으면 dto.getResponse(), 없으면 null로 넘김
        String reason = (dto != null ? dto.getReason() : null);
        orderService.cancelOrder(orderId, reason);
        return new SuccessResponseDto("주문이 정상적으로 취소되었습니다.");
    }

    @GetMapping("/{orderId}/status-log")
    public List<OrderStatusLogDto> getStatusLog(@PathVariable Long orderId) {
    // getStatusLog는 특정 orderId에 대한 해당 주문의 상태 변경 로그만 가져오는 메서드 이다.
        return orderService.getStatusLog(orderId);
    }

    @PostMapping("/{orderId}/returns")
    public int requestReturn(@PathVariable Long orderId) {
        // 반품 처리 후 "환불 금액" (int)만 바로 리턴
        return orderService.requestReturn(orderId);
    }

    // validateOrderType 메서드는 컨트롤러에서 들어온 주문 요청(OrderRequestDto)
    //이 올바른 형태인지를 추가로 검사하기 위해 만든 "수동 유효성 검사" 이다.
    private void validateOrderType(OrderRequestDto req) {
        String type = req.getOrderType();
        if ("guest".equalsIgnoreCase(type)) {
            if (req.getGuestName() == null || req.getGuestPhone() == null) {
                throw new IllegalArgumentException("비회원 주문은 이름과 전화번호가 필요합니다.");
            }
        } else if ("member".equalsIgnoreCase(type)) {
            if (req.getUserId() == null) {
                throw new IllegalArgumentException("회원 주문은 userId가 필요합니다.");
            }
        } else {
            throw new IllegalArgumentException("orderType은 'member' 또는 'guest'여야 합니다.");
        }
    }
}