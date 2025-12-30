package com.abhishek.ecommerce.cart.service;

import com.abhishek.ecommerce.cart.dto.request.AddToCartRequestDto;
import com.abhishek.ecommerce.cart.dto.request.UpdateCartItemRequestDto;
import com.abhishek.ecommerce.cart.dto.response.CartResponseDto;

public interface CartService {

    CartResponseDto getCartByUserId(Long userId);

    CartResponseDto addProduct(Long userId, AddToCartRequestDto requestDto);

    CartResponseDto updateQuantity(Long userId, Long productId, UpdateCartItemRequestDto requestDto);

    void removeProduct(Long userId, Long productId);

    void clearCart(Long userId);
}

