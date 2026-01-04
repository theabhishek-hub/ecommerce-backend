package com.abhishek.ecommerce.cart.service;

import com.abhishek.ecommerce.cart.dto.request.AddToCartRequestDto;
import com.abhishek.ecommerce.cart.dto.request.UpdateCartItemRequestDto;
import com.abhishek.ecommerce.cart.dto.response.CartResponseDto;

public interface CartService {

    CartResponseDto getCartByUserId(Long userId);

    /**
     * Get cart for the current authenticated user
     */
    CartResponseDto getCartForCurrentUser();

    CartResponseDto addProduct(Long userId, AddToCartRequestDto requestDto);

    /**
     * Add product to cart for the current authenticated user
     */
    CartResponseDto addProductForCurrentUser(AddToCartRequestDto requestDto);

    CartResponseDto updateQuantity(Long userId, Long productId, UpdateCartItemRequestDto requestDto);

    /**
     * Update cart item quantity for the current authenticated user
     */
    CartResponseDto updateQuantityForCurrentUser(Long productId, UpdateCartItemRequestDto requestDto);

    void removeProduct(Long userId, Long productId);

    /**
     * Remove product from cart for the current authenticated user
     */
    void removeProductForCurrentUser(Long productId);

    void clearCart(Long userId);

    /**
     * Clear cart for the current authenticated user
     */
    void clearCartForCurrentUser();
}

