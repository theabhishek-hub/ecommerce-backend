package com.abhishek.ecommerce.payment.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.payment.dto.request.PaymentCreateRequestDto;
import com.abhishek.ecommerce.payment.dto.response.PaymentResponseDto;
import com.abhishek.ecommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payments", description = "Payment processing APIs")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ========================= CREATE =========================
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PaymentResponseDto> createPayment(
            @Valid @RequestBody PaymentCreateRequestDto requestDto
    ) {
        PaymentResponseDto response = paymentService.createPayment(requestDto);
        return ApiResponseBuilder.created("Payment created successfully", response);
    }

    // ========================= GET BY ID =========================
    @GetMapping("/{paymentId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated() and (@paymentSecurity.isPaymentOwner(#paymentId) or hasRole('ADMIN'))")
    public ApiResponse<PaymentResponseDto> getPaymentById(@PathVariable Long paymentId) {
        PaymentResponseDto response = paymentService.getPaymentById(paymentId);
        return ApiResponseBuilder.success("Payment fetched successfully", response);
    }

    // ========================= GET BY ORDER ID =========================
    @GetMapping("/orders/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated() and (@paymentSecurity.isOrderOwnerForPayment(#orderId) or hasRole('ADMIN'))")
    public ApiResponse<PaymentResponseDto> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentResponseDto response = paymentService.getPaymentByOrderId(orderId);
        return ApiResponseBuilder.success("Payment fetched successfully", response);
    }

    // ========================= MARK SUCCESS =========================
    @PutMapping("/{paymentId}/success")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PaymentResponseDto> markPaymentSuccess(@PathVariable Long paymentId) {
        PaymentResponseDto response = paymentService.markPaymentSuccess(paymentId);
        return ApiResponseBuilder.success("Payment marked success", response);
    }

    // ========================= REFUND =========================
    @PutMapping("/refund/{paymentId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PaymentResponseDto> refundPayment(@PathVariable Long paymentId) {
        PaymentResponseDto response = paymentService.refundPayment(paymentId);
        return ApiResponseBuilder.success("Payment refunded successfully", response);
    }
}
