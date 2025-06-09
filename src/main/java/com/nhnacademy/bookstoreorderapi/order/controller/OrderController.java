package com.nhnacademy.bookstoreorderapi.order.controller;

import com.nhnacademy.bookstoreorderapi.order.dto.OrderRequestDto;
import com.nhnacademy.bookstoreorderapi.order.dto.OrderResponseDto;
import com.nhnacademy.bookstoreorderapi.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createOrderJson(@Valid @RequestBody OrderRequestDto req) {
        try {
            validateOrderType(req);
            OrderResponseDto resp = orderService.createOrder(req);
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ErrorMessage("주문 실패: " + ex.getMessage()));
        }
    }

    @GetMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<OrderResponseDto> listAllJson() {
        return orderService.listAll();
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String listAllHtml(Model model) {
        model.addAttribute("orders", orderService.listAll());
        model.addAttribute("orderForm", new OrderRequestDto());
        return "orders";
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String submitOrderHtml(
            @Valid @ModelAttribute("orderForm") OrderRequestDto orderForm,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("orders", orderService.listAll());
            model.addAttribute("errorMessage", "입력값이 유효하지 않습니다.");
            return "orders";
        }

        try {
            validateOrderType(orderForm);
            orderService.createOrder(orderForm);
            return "redirect:/orders";
        } catch (Exception ex) {
            model.addAttribute("orders", orderService.listAll());
            model.addAttribute("errorMessage", ex.getMessage());
            return "orders";
        }
    }

    private void validateOrderType(OrderRequestDto req) {
        if ("guest".equalsIgnoreCase(req.getOrderType())) {
            if (req.getGuestName() == null || req.getGuestPhone() == null) {
                throw new IllegalArgumentException("비회원 주문은 이름과 전화번호가 필요합니다.");
            }
        } else if ("member".equalsIgnoreCase(req.getOrderType())) {
            if (req.getUserId() == null) {
                throw new IllegalArgumentException("회원 주문은 userId가 필요합니다.");
            }
        } else {
            throw new IllegalArgumentException("orderType은 'member' 또는 'guest'여야 합니다.");
        }
    }

    record ErrorMessage(String message) {}
}