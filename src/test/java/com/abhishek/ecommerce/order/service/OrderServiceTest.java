package com.abhishek.ecommerce.order.service;

import com.abhishek.ecommerce.cart.entity.Cart;
import com.abhishek.ecommerce.cart.entity.CartItem;
import com.abhishek.ecommerce.cart.repository.CartRepository;
import com.abhishek.ecommerce.common.baseEntity.Money;
import com.abhishek.ecommerce.shared.enums.PaymentStatus;
import com.abhishek.ecommerce.inventory.dto.request.UpdateStockRequestDto;
import com.abhishek.ecommerce.inventory.service.InventoryService;
import com.abhishek.ecommerce.notification.NotificationService;
import com.abhishek.ecommerce.order.dto.response.OrderResponseDto;
import com.abhishek.ecommerce.order.entity.Order;
import com.abhishek.ecommerce.shared.enums.OrderStatus;
import com.abhishek.ecommerce.order.exception.OrderNotFoundException;
import com.abhishek.ecommerce.order.mapper.OrderMapper;
import com.abhishek.ecommerce.order.repository.OrderRepository;
import com.abhishek.ecommerce.order.service.impl.OrderServiceImpl;
import com.abhishek.ecommerce.payment.service.PaymentService;
import com.abhishek.ecommerce.product.entity.Product;
import com.abhishek.ecommerce.common.utils.SecurityUtils;
import com.abhishek.ecommerce.shared.enums.Role;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Cart cart;
    private Order order;
    private OrderResponseDto orderResponseDto;
    private com.abhishek.ecommerce.payment.dto.response.PaymentResponseDto paymentResponseDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("john.doe@example.com");
        user.setFullName("John Doe");
        user.setRoles(Collections.singleton(Role.ROLE_ADMIN));

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new Money(BigDecimal.valueOf(99.99), "USD"));

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setPrice(new Money(BigDecimal.valueOf(99.99), "USD"));

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>(List.of(cartItem)));

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);
        order.setItems(new ArrayList<>());

        orderResponseDto = new OrderResponseDto();
        orderResponseDto.setId(1L);
        orderResponseDto.setUserId(1L);
        orderResponseDto.setStatus("CREATED");

        paymentResponseDto = new com.abhishek.ecommerce.payment.dto.response.PaymentResponseDto();
        paymentResponseDto.setId(1L);
        paymentResponseDto.setStatus(PaymentStatus.SUCCESS);
    }

    @Test
    void placeOrder_ShouldCreateOrderSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);
        when(inventoryService.reduceStock(anyLong(), any(UpdateStockRequestDto.class))).thenReturn(null);

        // When
        OrderResponseDto result = orderService.placeOrder(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("CREATED");

        verify(userRepository).findById(1L);
        verify(cartRepository).findByUserId(1L);
        verify(inventoryService).reduceStock(anyLong(), any(UpdateStockRequestDto.class));
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).toDto(order);
    }

    @Test
    void placeOrder_ShouldThrowException_WhenCartIsEmpty() {
        // Given
        cart.getItems().clear();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        // When & Then
        assertThatThrownBy(() -> orderService.placeOrder(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot place order with empty cart");

        verify(userRepository).findById(1L);
        verify(cartRepository).findByUserId(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_ShouldReturnOrder() {
        // Given
        when(securityUtils.getCurrentUsername()).thenReturn("john.doe@example.com");
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);

        // When
        OrderResponseDto result = orderService.getOrderById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(securityUtils).getCurrentUsername();
        verify(userRepository).findByEmail("john.doe@example.com");
        verify(orderRepository).findById(1L);
        verify(orderMapper).toDto(order);
    }

    @Test
    void getOrderById_ShouldThrowException_WhenOrderNotFound() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderById(1L))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository).findById(1L);
        verify(orderMapper, never()).toDto(any(Order.class));
    }

    @Test
    void getOrdersByUser_ShouldReturnUserOrders() {
        // Given
        List<Order> orders = List.of(order);
        when(orderRepository.findByUserId(1L)).thenReturn(orders);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderResponseDto);

        // When
        List<OrderResponseDto> result = orderService.getOrdersByUser(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(orderRepository).findByUserId(1L);
        verify(orderMapper).toDto(any(Order.class));
    }

    @Test
    void shipOrder_ShouldUpdateOrderStatusToShipped() {
        // Given
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);
        doNothing().when(notificationService).sendOrderShippedNotification(anyLong(), anyString(), anyString(), anyString());

        // When
        OrderResponseDto result = orderService.shipOrder(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).toDto(order);
    }

    @Test
    void deliverOrder_ShouldUpdateOrderStatusToDelivered() {
        // Given
        order.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);
        doNothing().when(notificationService).sendOrderDeliveredNotification(anyLong(), anyString(), anyString());

        // When
        OrderResponseDto result = orderService.deliverOrder(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).toDto(order);
    }

    @Test
    void cancelOrder_ShouldUpdateOrderStatusToCancelled() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);

        // When
        OrderResponseDto result = orderService.cancelOrder(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).toDto(order);
    }

    @Test
    void placeOrderForCurrentUser_ShouldPlaceOrderForCurrentUser() {
        // Given
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);

        // When
        OrderResponseDto result = orderService.placeOrderForCurrentUser();

        // Then
        assertThat(result).isNotNull();
        verify(securityUtils).getCurrentUserId();
        verify(userRepository).findById(1L);
        verify(cartRepository).findByUserId(1L);
    }
}