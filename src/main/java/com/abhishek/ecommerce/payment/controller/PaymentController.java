package com.abhishek.ecommerce.payment.controller;

import com.abhishek.ecommerce.payment.entity.Payment;
import com.abhishek.ecommerce.payment.entity.PaymentMethod;
import com.abhishek.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public Payment initiatePayment(
            @RequestParam Long orderId,
            @RequestParam PaymentMethod method
    ) {
        return paymentService.initiatePayment(orderId, method);
    }

    @GetMapping("/{paymentId}")
    public Payment getPaymentById(@PathVariable Long paymentId) {
        return paymentService.getPaymentById(paymentId);
    }

    @GetMapping("/order/{orderId}")
    public Payment getPaymentByOrderId(@PathVariable Long orderId) {
        return paymentService.getPaymentByOrderId(orderId);
    }

    @PutMapping("/{paymentId}/success")
    public Payment markPaymentSuccess(@PathVariable Long paymentId) {
        return paymentService.markPaymentSuccess(paymentId);
    }

    @PutMapping("/refund/order/{orderId}")
    public Payment refundPayment(@PathVariable Long orderId) {
        return paymentService.refundPayment(orderId);
    }
}

