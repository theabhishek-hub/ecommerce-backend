package com.abhishek.ecommerce.cart.service.impl;

import com.abhishek.ecommerce.cart.entity.Cart;
import com.abhishek.ecommerce.cart.entity.CartItem;
import com.abhishek.ecommerce.cart.repository.CartItemRepository;
import com.abhishek.ecommerce.cart.repository.CartRepository;
import com.abhishek.ecommerce.cart.service.CartService;
import com.abhishek.ecommerce.product.entity.Product;
import com.abhishek.ecommerce.product.repository.ProductRepository;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

//    public CartServiceImpl(
//            CartRepository cartRepository,
//            CartItemRepository cartItemRepository,
//            UserRepository userRepository,
//            ProductRepository productRepository) {
//        this.cartRepository = cartRepository;
//        this.cartItemRepository = cartItemRepository;
//        this.userRepository = userRepository;
//        this.productRepository = productRepository;
//    }

    @Override
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
    }

    @Override
    public Cart addProduct(Long userId, Long productId, Integer quantity) {

        Cart cart = getCartByUserId(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setPrice(product.getPrice());
            item.setQuantity(quantity);
            cart.getItems().add(item);
        } else {
            item.setQuantity(item.getQuantity() + quantity);
        }

        return cartRepository.save(cart);
    }

    @Override
    public Cart updateQuantity(Long userId, Long productId, Integer quantity) {

        Cart cart = getCartByUserId(userId);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setQuantity(quantity);
        return cart;
    }

    @Override
    public void removeProduct(Long userId, Long productId) {

        Cart cart = getCartByUserId(userId);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        cart.getItems().remove(item);
    }

    @Override
    public void clearCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().clear();
    }

    private Cart createCart(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

//    @Override
//    @Transactional
//    public Cart decreaseQuantity(Long userId, Long productId) {
//        Cart cart = getCartByUserId(userId);
//        Iterator<CartItem> iterator = cart.getItems().iterator();
//
//        while (iterator.hasNext()) {
//            CartItem item = iterator.next();
//
//            if (item.getProduct().getId().equals(productId)) {
//                // Decrease quantity by 1 (since the method is called decreaseQuantity)
//                if (item.getQuantity() > 1) {
//                    item.setQuantity(item.getQuantity() - 1);
//                } else {
//                    iterator.remove(); // Remove item if quantity would go to 0
//                }
//                break;
//            }
//        }
//
//        return cartRepository.save(cart);
//    }



}

