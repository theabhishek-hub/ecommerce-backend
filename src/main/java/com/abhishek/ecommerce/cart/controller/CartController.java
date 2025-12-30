package com.abhishek.ecommerce.cart.controller;

import com.abhishek.ecommerce.cart.dto.request.AddToCartRequestDto;
import com.abhishek.ecommerce.cart.dto.request.UpdateCartItemRequestDto;
import com.abhishek.ecommerce.cart.dto.response.CartResponseDto;
import com.abhishek.ecommerce.cart.service.CartService;
import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // ========================= ADD PRODUCT =========================
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CartResponseDto> addProduct(
            @PathVariable Long userId,
            @Valid @RequestBody AddToCartRequestDto requestDto) {
        CartResponseDto response = cartService.addProduct(userId, requestDto);
        return ApiResponseBuilder.success("Product added to cart successfully", response);
    }

    // ========================= GET CART =========================
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CartResponseDto> getCart(@PathVariable Long userId) {
        CartResponseDto response = cartService.getCartByUserId(userId);
        return ApiResponseBuilder.success("Cart fetched successfully", response);
    }

    // ========================= UPDATE QUANTITY =========================
    @PutMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CartResponseDto> updateQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequestDto requestDto) {
        CartResponseDto response = cartService.updateQuantity(userId, productId, requestDto);
        return ApiResponseBuilder.success("Cart updated successfully", response);
    }

    // ========================= REMOVE PRODUCT =========================
    @DeleteMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> removeProduct(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        cartService.removeProduct(userId, productId);
        return ApiResponseBuilder.success("Product removed from cart successfully", null);
    }

    // ========================= CLEAR CART =========================
    @DeleteMapping("/clear")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ApiResponseBuilder.success("Cart cleared successfully", null);
    }
}
