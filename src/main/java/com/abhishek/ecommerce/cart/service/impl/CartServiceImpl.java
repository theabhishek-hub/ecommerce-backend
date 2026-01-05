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
import com.abhishek.ecommerce.security.SecurityUtils;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.exception.UserNotFoundException;
import com.abhishek.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;
    private final SecurityUtils securityUtils;

    // ========================= READ =========================
    @Override
    @Transactional
    public CartResponseDto getCartByUserId(Long userId) {
        log.debug("getCartByUserId for userId={}", userId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
        return cartMapper.toDto(cart);
    }

    // ========================= ADD PRODUCT =========================
    @Override
    public CartResponseDto addProduct(Long userId, AddToCartRequestDto requestDto) {
        log.info("addProduct started for userId={} productId={}", userId, requestDto.getProductId());

        Cart cart = getCartEntityByUserId(userId);

        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(requestDto.getProductId()));

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
            log.info("addProduct new item cartId={} productId={} qty={}", cart.getId(), requestDto.getProductId(), requestDto.getQuantity());
        } else {
            item.setQuantity(item.getQuantity() + requestDto.getQuantity());
            log.info("addProduct updated item cartId={} productId={} qty={}", cart.getId(), requestDto.getProductId(), item.getQuantity());
        }

        Cart savedCart = cartRepository.save(cart);
        log.info("addProduct completed for userId={}", userId);
        return cartMapper.toDto(savedCart);
    }

    // ========================= UPDATE QUANTITY =========================
    @Override
    public CartResponseDto updateQuantity(Long userId, Long productId, UpdateCartItemRequestDto requestDto) {
        log.info("updateQuantity started for userId={} productId={} qty={}", userId, productId, requestDto.getQuantity());

        Cart cart = getCartEntityByUserId(userId);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));

        item.setQuantity(requestDto.getQuantity());
        cartRepository.save(cart);
        
        log.info("updateQuantity completed for userId={} productId={}", userId, productId);
        return cartMapper.toDto(cart);
    }

    // ========================= REMOVE PRODUCT =========================
    @Override
    public void removeProduct(Long userId, Long productId) {
        log.info("removeProduct started for userId={} productId={}", userId, productId);

        Cart cart = getCartEntityByUserId(userId);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));

        cart.getItems().remove(item);
        cartRepository.save(cart);

        log.info("removeProduct completed for userId={} productId={}", userId, productId);
    }

    // ========================= CLEAR CART =========================
    @Override
    public void clearCart(Long userId) {
        log.info("clearCart started for userId={}", userId);
        Cart cart = getCartEntityByUserId(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("clearCart completed for userId={}", userId);
    }

    // ========================= CURRENT USER METHODS =========================
    @Override
    @Transactional
    public CartResponseDto getCartForCurrentUser() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return getCartByUserId(userId);
    }

    @Override
    public CartResponseDto addProductForCurrentUser(AddToCartRequestDto requestDto) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return addProduct(userId, requestDto);
    }

    @Override
    public CartResponseDto updateQuantityForCurrentUser(Long productId, UpdateCartItemRequestDto requestDto) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return updateQuantity(userId, productId, requestDto);
    }

    @Override
    public void removeProductForCurrentUser(Long productId) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        removeProduct(userId, productId);
    }

    @Override
    public void clearCartForCurrentUser() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        clearCart(userId);
    }

    // ========================= PRIVATE HELPERS =========================
    private Cart getCartEntityByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
    }

    private Cart createCart(Long userId) {
        log.info("createCart for userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Cart cart = new Cart();
        cart.setUser(user);
        Cart savedCart = cartRepository.save(cart);
        log.info("createCart completed cartId={} userId={}", savedCart.getId(), userId);
        return savedCart;
    }
}

