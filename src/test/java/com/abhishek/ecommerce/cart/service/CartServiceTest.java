package com.abhishek.ecommerce.cart.service;

import com.abhishek.ecommerce.cart.dto.request.AddToCartRequestDto;
import com.abhishek.ecommerce.cart.dto.request.UpdateCartItemRequestDto;
import com.abhishek.ecommerce.cart.dto.response.CartResponseDto;
import com.abhishek.ecommerce.cart.entity.Cart;
import com.abhishek.ecommerce.cart.entity.CartItem;
import com.abhishek.ecommerce.cart.exception.CartItemNotFoundException;
import com.abhishek.ecommerce.cart.mapper.CartMapper;
import com.abhishek.ecommerce.cart.repository.CartItemRepository;
import com.abhishek.ecommerce.cart.repository.CartRepository;
import com.abhishek.ecommerce.cart.service.impl.CartServiceImpl;
import com.abhishek.ecommerce.common.baseEntity.Money;
import com.abhishek.ecommerce.product.entity.Product;
import com.abhishek.ecommerce.product.exception.ProductNotFoundException;
import com.abhishek.ecommerce.product.repository.ProductRepository;
import com.abhishek.ecommerce.common.utils.SecurityUtils;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cart;
    private CartItem cartItem;
    private User user;
    private Product product;
    private CartResponseDto cartResponseDto;
    private AddToCartRequestDto addToCartRequestDto;
    private UpdateCartItemRequestDto updateCartItemRequestDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("john.doe@example.com");

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new Money(BigDecimal.valueOf(99.99), "USD"));

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setPrice(new Money(BigDecimal.valueOf(99.99), "USD"));
        cartItem.setQuantity(2);

        cartResponseDto = new CartResponseDto();
        cartResponseDto.setId(1L);
        cartResponseDto.setUserId(1L);

        addToCartRequestDto = new AddToCartRequestDto();
        addToCartRequestDto.setProductId(1L);
        addToCartRequestDto.setQuantity(2);

        updateCartItemRequestDto = new UpdateCartItemRequestDto();
        updateCartItemRequestDto.setQuantity(5);
    }

    @Test
    void getCartByUserId_ShouldReturnExistingCart() {
        // Given
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartMapper.toDto(cart)).thenReturn(cartResponseDto);

        // When
        CartResponseDto result = cartService.getCartByUserId(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);

        verify(cartRepository).findByUserId(1L);
        verify(cartMapper).toDto(cart);
    }

    @Test
    void getCartByUserId_ShouldCreateNewCart_WhenCartDoesNotExist() {
        // Given
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartMapper.toDto(cart)).thenReturn(cartResponseDto);

        // When
        CartResponseDto result = cartService.getCartByUserId(1L);

        // Then
        assertThat(result).isNotNull();
        verify(cartRepository).findByUserId(1L);
        verify(userRepository).findById(1L);
        verify(cartRepository).save(any(Cart.class));
        verify(cartMapper).toDto(cart);
    }

    @Test
    void addProduct_ShouldAddNewProductToCart() {
        // Given
        cart.getItems().clear();
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartMapper.toDto(cart)).thenReturn(cartResponseDto);

        // When
        CartResponseDto result = cartService.addProduct(1L, addToCartRequestDto);

        // Then
        assertThat(result).isNotNull();
        verify(cartRepository).findByUserId(1L);
        verify(productRepository).findById(1L);
        verify(cartItemRepository).findByCartIdAndProductId(1L, 1L);
        verify(cartRepository).save(any(Cart.class));
        verify(cartMapper).toDto(cart);
    }

    @Test
    void addProduct_ShouldUpdateExistingProductQuantity() {
        // Given
        cart.getItems().add(cartItem);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(cartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartMapper.toDto(cart)).thenReturn(cartResponseDto);

        // When
        CartResponseDto result = cartService.addProduct(1L, addToCartRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(cartItem.getQuantity()).isEqualTo(4); // 2 + 2
        verify(cartRepository).findByUserId(1L);
        verify(productRepository).findById(1L);
        verify(cartItemRepository).findByCartIdAndProductId(1L, 1L);
        verify(cartRepository).save(any(Cart.class));
        verify(cartMapper).toDto(cart);
    }

    @Test
    void addProduct_ShouldThrowException_WhenProductNotFound() {
        // Given
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.addProduct(1L, addToCartRequestDto))
                .isInstanceOf(ProductNotFoundException.class);

        verify(cartRepository).findByUserId(1L);
        verify(productRepository).findById(1L);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void updateQuantity_ShouldUpdateCartItemQuantity() {
        // Given
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(cartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartMapper.toDto(cart)).thenReturn(cartResponseDto);

        // When
        CartResponseDto result = cartService.updateQuantity(1L, 1L, updateCartItemRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(cartItem.getQuantity()).isEqualTo(5);
        verify(cartRepository).findByUserId(1L);
        verify(cartItemRepository).findByCartIdAndProductId(1L, 1L);
        verify(cartRepository).save(any(Cart.class));
        verify(cartMapper).toDto(cart);
    }

    @Test
    void updateQuantity_ShouldThrowException_WhenCartItemNotFound() {
        // Given
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.updateQuantity(1L, 1L, updateCartItemRequestDto))
                .isInstanceOf(CartItemNotFoundException.class);

        verify(cartRepository).findByUserId(1L);
        verify(cartItemRepository).findByCartIdAndProductId(1L, 1L);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void removeProduct_ShouldRemoveProductFromCart() {
        // Given
        cart.getItems().add(cartItem);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(cartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        cartService.removeProduct(1L, 1L);

        // Then
        assertThat(cart.getItems()).doesNotContain(cartItem);
        verify(cartRepository).findByUserId(1L);
        verify(cartItemRepository).findByCartIdAndProductId(1L, 1L);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void clearCart_ShouldRemoveAllItemsFromCart() {
        // Given
        cart.getItems().add(cartItem);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        cartService.clearCart(1L);

        // Then
        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).findByUserId(1L);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getCartForCurrentUser_ShouldReturnCartForCurrentUser() {
        // Given
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartMapper.toDto(cart)).thenReturn(cartResponseDto);

        // When
        CartResponseDto result = cartService.getCartForCurrentUser();

        // Then
        assertThat(result).isNotNull();
        verify(securityUtils).getCurrentUserId();
        verify(cartRepository).findByUserId(1L);
        verify(cartMapper).toDto(cart);
    }

    @Test
    void getCartForCurrentUser_ShouldThrowException_WhenUserNotAuthenticated() {
        // Given
        when(securityUtils.getCurrentUserId()).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> cartService.getCartForCurrentUser())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User not authenticated");

        verify(securityUtils).getCurrentUserId();
        verify(cartRepository, never()).findByUserId(anyLong());
    }
}