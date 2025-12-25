package com.abhishek.ecommerce.cart.service;

import com.abhishek.ecommerce.cart.entity.Cart;

public interface CartService {

    Cart getCartByUserId(Long userId);

    Cart addProduct(Long userId, Long productId, Integer quantity);

    Cart updateQuantity(Long userId, Long productId, Integer quantity);

    void removeProduct(Long userId, Long productId);

    void clearCart(Long userId);

}

