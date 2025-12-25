package com.abhishek.ecommerce.cart.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.ApiResponseBuilder;
import com.abhishek.ecommerce.cart.entity.Cart;
import com.abhishek.ecommerce.cart.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ApiResponse<Cart> getCart(@PathVariable Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        return ApiResponseBuilder.success("Cart fetched successfully", cart);
    }

    @PostMapping("/products")
    public ApiResponse<Cart> addProduct(
            @PathVariable Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        Cart cart = cartService.addProduct(userId, productId, quantity);
        return ApiResponseBuilder.success("Product added to cart successfully", cart);
    }

    @PutMapping("/products/{productId}")
    public ApiResponse<Cart> updateQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        Cart cart = cartService.updateQuantity(userId, productId, quantity);
        return ApiResponseBuilder.success("Cart updated successfully", cart);
    }

    @DeleteMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> removeProduct(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        cartService.removeProduct(userId, productId);
        return ApiResponseBuilder.success("Product removed from cart successfully");
    }

    @DeleteMapping("/clear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> clearCart(@PathVariable Long userId)
    {
        cartService.clearCart(userId);
        return ApiResponseBuilder.success("Cart cleared successfully");
    }


}
