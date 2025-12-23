package com.abhishek.ecommerce.cart.controller;

import com.abhishek.ecommerce.cart.entity.Cart;
import com.abhishek.ecommerce.cart.service.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userId}")
    public Cart getCart(@PathVariable Long userId) {
        return cartService.getCartByUserId(userId);
    }

    @PostMapping("/{userId}/add")
    public Cart addProduct(
            @PathVariable Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        return cartService.addProduct(userId, productId, quantity);
    }

    @PutMapping("/{userId}/update")
    public Cart updateQuantity(
            @PathVariable Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        return cartService.updateQuantity(userId, productId, quantity);
    }

    @DeleteMapping("/{userId}/remove/{productId}")
    public void removeProduct(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        cartService.removeProduct(userId, productId);
    }

    @DeleteMapping("/{userId}/clear")
    public void clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
    }

    @PostMapping("/{userId}/decrease")
    public Cart decreaseQuantity(
            @PathVariable Long userId,
            @RequestParam Long productId
    ) {
        return cartService.decreaseQuantity(userId, productId);
    }

}

