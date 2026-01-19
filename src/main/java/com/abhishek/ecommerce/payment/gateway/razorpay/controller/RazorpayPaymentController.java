package com.abhishek.ecommerce.payment.gateway.razorpay.controller;

import com.abhishek.ecommerce.common.apiResponse.ApiResponse;
import com.abhishek.ecommerce.common.apiResponse.ApiResponseBuilder;
import com.abhishek.ecommerce.payment.gateway.razorpay.RazorpayProperties;
import com.abhishek.ecommerce.payment.gateway.razorpay.dto.request.RazorpayCreateOrderRequestDto;
import com.abhishek.ecommerce.payment.gateway.razorpay.dto.request.RazorpayVerifyPaymentRequestDto;
import com.abhishek.ecommerce.payment.gateway.razorpay.dto.response.RazorpayCreateOrderResponseDto;
import com.abhishek.ecommerce.payment.gateway.razorpay.dto.response.RazorpayEnabledResponseDto;
import com.abhishek.ecommerce.payment.gateway.razorpay.service.RazorpayPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Razorpay Payments", description = "Razorpay sandbox/test-mode payment APIs")
@RestController
@RequestMapping("/api/v1/payments/razorpay")
@RequiredArgsConstructor
public class RazorpayPaymentController {

    private final RazorpayPaymentService razorpayPaymentService;
    private final RazorpayProperties razorpayProperties;

    @GetMapping("/enabled")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<RazorpayEnabledResponseDto> enabled() {
        boolean enabled = razorpayPaymentService.isEnabled();
        return ApiResponseBuilder.success(
                "Razorpay capability fetched",
                RazorpayEnabledResponseDto.builder()
                        .enabled(enabled)
                        .keyId(enabled ? razorpayProperties.getKeyId() : null)
                        .build()
        );
    }

    @PostMapping("/create-order")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<RazorpayCreateOrderResponseDto> createOrder(@Valid @RequestBody RazorpayCreateOrderRequestDto requestDto) {
        RazorpayCreateOrderResponseDto response = razorpayPaymentService.createRazorpayOrder(requestDto);
        return ApiResponseBuilder.created("Razorpay order created", response);
    }

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> verify(@Valid @RequestBody RazorpayVerifyPaymentRequestDto requestDto) {
        razorpayPaymentService.verifyAndMarkPaid(requestDto);
        return ApiResponseBuilder.success("Payment verified and order marked paid", null);
    }
}

