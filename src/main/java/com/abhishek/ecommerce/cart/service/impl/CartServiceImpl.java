package com.abhishek.ecommerce.cart.service.impl;

import com.abhishek.ecommerce.cart.dto.request.AddToCartRequestDto;
import com.abhishek.ecommerce.cart.dto.request.UpdateCartItemRequestDto;
import com.abhishek.ecommerce.cart.dto.response.CartResponseDto;
import com.abhishek.ecommerce.cart.entity.Cart;
import com.abhishek.ecommerce.cart.entity.CartItem;
import com.abhishek.ecommerce.cart.exception.CartItemNotFoundException;
import com.abhishek.ecommerce.cart.mapper.CartMapper;
import com.abhishek.ecommerce.cart.repository.CartItemRepository;
import com.abhishek.ecommerce.cart.repository.CartRepository;
import com.abhishek.ecommerce.cart.service.CartService;
import com.abhishek.ecommerce.product.entity.Product;
import com.abhishek.ecommerce.product.exception.ProductNotFoundException;
import com.abhishek.ecommerce.product.repository.ProductRepository;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.exception.UserNotFoundException;
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
    private final CartMapper cartMapper;

    // ========================= READ =========================
    @Override
    @Transactional(readOnly = true)
    public CartResponseDto getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
        return cartMapper.toDto(cart);
    }

    // ========================= ADD PRODUCT =========================
    @Override
    public CartResponseDto addProduct(Long userId, AddToCartRequestDto requestDto) {

        Cart cart = getCartEntityByUserId(userId);

        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + requestDto.getProductId()));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), requestDto.getProductId())
                .orElse(null);

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setPrice(product.getPrice());
            item.setQuantity(requestDto.getQuantity());
            cart.getItems().add(item);
        } else {
            item.setQuantity(item.getQuantity() + requestDto.getQuantity());
        }

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toDto(savedCart);
    }

    // ========================= UPDATE QUANTITY =========================
    @Override
    public CartResponseDto updateQuantity(Long userId, Long productId, UpdateCartItemRequestDto requestDto) {

        Cart cart = getCartEntityByUserId(userId);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found for product id: " + productId));

        item.setQuantity(requestDto.getQuantity());
        cartRepository.save(cart);
        
        return cartMapper.toDto(cart);
    }

    // ========================= REMOVE PRODUCT =========================
    @Override
    public void removeProduct(Long userId, Long productId) {

        Cart cart = getCartEntityByUserId(userId);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found for product id: " + productId));

        cart.getItems().remove(item);
        cartRepository.save(cart);
    }

    // ========================= CLEAR CART =========================
    @Override
    public void clearCart(Long userId) {
        Cart cart = getCartEntityByUserId(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // ========================= PRIVATE HELPERS =========================
    private Cart getCartEntityByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
    }

    private Cart createCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }
}

