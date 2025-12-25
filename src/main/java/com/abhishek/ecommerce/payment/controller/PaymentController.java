package com.abhishek.ecommerce.payment.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.payment.entity.Payment;
import com.abhishek.ecommerce.payment.entity.PaymentMethod;
import com.abhishek.ecommerce.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Payment> createPayment(
            @RequestParam Long orderId,
            @RequestParam PaymentMethod method
    ) {
        return ApiResponseBuilder.success("Payment created successfully", paymentService.createPayment(orderId, method));
    }

    @GetMapping("/{paymentId}")
    public ApiResponse<Payment> getPaymentById(@PathVariable Long paymentId) {
        return ApiResponseBuilder.success("Payment fetched successfully", paymentService.getPaymentById(paymentId));
    }

    @GetMapping("/orders/{orderId}")
    public ApiResponse<Payment> getPaymentByOrderId(@PathVariable Long orderId) {
        return ApiResponseBuilder.success("Payment fetched successfully", paymentService.getPaymentByOrderId(orderId));
    }

    @PatchMapping("/{paymentId}/success")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Payment> markPaymentSuccess(@PathVariable Long paymentId) {
        return ApiResponseBuilder.success("Payment marked success", paymentService.markPaymentSuccess(paymentId));
    }

    @PatchMapping("/refund/{paymentId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Payment> refundPayment(@PathVariable Long paymentId) {
        return ApiResponseBuilder.success("Payment refunded successfully", paymentService.refundPayment(paymentId));
    }
}
