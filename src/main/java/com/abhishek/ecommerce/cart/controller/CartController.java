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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // ========================= GET CART =========================
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CartResponseDto> getCart() {
        CartResponseDto response = cartService.getCartForCurrentUser();
        return ApiResponseBuilder.success("Cart fetched successfully", response);
    }

    // ========================= ADD PRODUCT =========================
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CartResponseDto> addProduct(
            @Valid @RequestBody AddToCartRequestDto requestDto) {
        CartResponseDto response = cartService.addProductForCurrentUser(requestDto);
        return ApiResponseBuilder.success("Product added to cart successfully", response);
    }

    // ========================= UPDATE QUANTITY =========================
    @PutMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CartResponseDto> updateQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequestDto requestDto) {
        CartResponseDto response = cartService.updateQuantityForCurrentUser(productId, requestDto);
        return ApiResponseBuilder.success("Cart updated successfully", response);
    }

    // ========================= REMOVE PRODUCT =========================
    @DeleteMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> removeProduct(@PathVariable Long productId) {
        cartService.removeProductForCurrentUser(productId);
        return ApiResponseBuilder.success("Product removed from cart successfully", null);
    }

    // ========================= CLEAR CART =========================
    @DeleteMapping("/clear")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> clearCart() {
        cartService.clearCartForCurrentUser();
        return ApiResponseBuilder.success("Cart cleared successfully", null);
    }
}
