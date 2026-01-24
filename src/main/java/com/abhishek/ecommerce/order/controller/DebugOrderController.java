package com.abhishek.ecommerce.order.controller;

import com.abhishek.ecommerce.order.dto.response.OrderResponseDto;
import com.abhishek.ecommerce.order.service.OrderService;
import com.abhishek.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/debug")
@RequiredArgsConstructor
public class DebugOrderController {

    private final OrderService orderService;
    private final PaymentRepository paymentRepository;

    @GetMapping("/orders/{orderId}")
    public OrderResponseDto debugOrder(@PathVariable Long orderId) {
        log.info("[DEBUG] Fetching order {} from database", orderId);
        
        // Fetch the order
        OrderResponseDto order = orderService.getOrderById(orderId);
        log.info("[DEBUG] Order fetched: id={}, status={}, paymentMethod={}, paymentStatus={}", 
                order.getId(), order.getStatus(), order.getPaymentMethod(), order.getPaymentStatus());
        
        // Check if payment exists directly
        var paymentOpt = paymentRepository.findByOrderId(orderId);
        if (paymentOpt.isPresent()) {
            var payment = paymentOpt.get();
            log.info("[DEBUG] Payment found in DB: id={}, method={}, status={}", 
                    payment.getId(), payment.getPaymentMethod(), payment.getStatus());
        } else {
            log.warn("[DEBUG] No payment found for order {}", orderId);
        }
        
        return order;
    }
}
