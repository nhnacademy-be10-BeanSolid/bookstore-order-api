package com.nhnacademy.bookstoreorderapi.payment.controller;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.Order;
import com.nhnacademy.bookstoreorderapi.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PaymentViewController {
    private final OrderRepository orderRepo;

    /**
     * 주문 결제 화면
     * - URL: /payments/toss/{orderId}
     * - Model 에 orderId와 totalPrice를 담아서 Thymeleaf 로 렌더링
     */
    @GetMapping("/payments/toss/{orderId}")
    public String paymentPage(@PathVariable String orderId, Model model) {
        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 없음: " + orderId));

        model.addAttribute("orderId",    orderId);
        model.addAttribute("totalPrice", order.getTotalPrice());
        return "paymentForm";   // → src/main/resources/templates/payment.html
    }
}