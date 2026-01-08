package com.abhishek.ecommerce.order.service.impl;

import com.abhishek.ecommerce.cart.entity.Cart;
import com.abhishek.ecommerce.cart.entity.CartItem;
import com.abhishek.ecommerce.cart.exception.CartNotFoundException;
import com.abhishek.ecommerce.cart.repository.CartRepository;
import com.abhishek.ecommerce.common.api.PageResponseDto;
import com.abhishek.ecommerce.inventory.service.InventoryService;
import com.abhishek.ecommerce.order.dto.response.OrderResponseDto;
import com.abhishek.ecommerce.order.entity.Order;
import com.abhishek.ecommerce.order.entity.OrderItem;
import com.abhishek.ecommerce.order.entity.OrderStatus;
import com.abhishek.ecommerce.order.exception.OrderNotFoundException;
import com.abhishek.ecommerce.order.mapper.OrderMapper;
import com.abhishek.ecommerce.order.repository.OrderRepository;
import com.abhishek.ecommerce.order.service.OrderService;
import com.abhishek.ecommerce.payment.entity.PaymentMethod;
import com.abhishek.ecommerce.payment.service.PaymentService;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.entity.Role;
import com.abhishek.ecommerce.user.exception.UserNotFoundException;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.abhishek.ecommerce.common.entity.Money;
import com.abhishek.ecommerce.security.SecurityUtils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional   // 🔥 VERY IMPORTANT
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final OrderMapper orderMapper;
    private final SecurityUtils securityUtils;


    @Override
    public OrderResponseDto placeOrder(Long userId) {
        log.info("placeOrder started for userId={}", userId);

        // 1️⃣ Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 2️⃣ Fetch cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));

        if (cart.getItems().isEmpty()) {
            log.warn("placeOrder empty cart for userId={}", userId);
            throw new RuntimeException("Cannot place order with empty cart");
        }

        // 3️⃣ Create Order
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);

        BigDecimal total = BigDecimal.ZERO;

        // 4️⃣ Convert cart items → order items + REDUCE INVENTORY
        for (CartItem cartItem : cart.getItems()) {

            // 🔥 THIS WAS MISSING
            com.abhishek.ecommerce.inventory.dto.request.UpdateStockRequestDto stockRequest = 
                new com.abhishek.ecommerce.inventory.dto.request.UpdateStockRequestDto();
            stockRequest.setQuantity(cartItem.getQuantity());
            inventoryService.reduceStock(cartItem.getProduct().getId(), stockRequest);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());

            order.getItems().add(orderItem);

            total = total.add(
                    cartItem.getPrice().getAmount()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
        }

        // 5️⃣ Set total amount
        order.setTotalAmount(new Money(total, "INR"));

        // 6️⃣ Save order
        Order savedOrder = orderRepository.save(order);
        log.info("placeOrder completed orderId={} userId={}", savedOrder.getId(), userId);

        // Create payment entry (COD for now)
        com.abhishek.ecommerce.payment.dto.request.PaymentCreateRequestDto paymentRequest = 
            new com.abhishek.ecommerce.payment.dto.request.PaymentCreateRequestDto();
        paymentRequest.setOrderId(savedOrder.getId());
        paymentRequest.setPaymentMethod(PaymentMethod.COD);
        paymentService.createPayment(paymentRequest);

        // 7️⃣ Clear cart SAFELY
        cart.getItems().clear();
        cartRepository.save(cart);

        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<OrderResponseDto> getOrdersByUser(Long userId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);
        List<OrderResponseDto> content = orderPage.getContent()
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());

        return PageResponseDto.<OrderResponseDto>builder()
                .content(content)
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .first(orderPage.isFirst())
                .last(orderPage.isLast())
                .empty(orderPage.isEmpty())
                .build();
    }

    @Override
    public OrderResponseDto shipOrder(Long orderId) {
        log.info("shipOrder started for orderId={}", orderId);
        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.PAID) {
            log.warn("shipOrder invalid status orderId={} status={}", orderId, order.getStatus());
            throw new IllegalStateException("Only PAID orders can be shipped");
        }

        order.setStatus(OrderStatus.SHIPPED);
        Order savedOrder = orderRepository.save(order);
        log.info("shipOrder completed orderId={}", orderId);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public OrderResponseDto deliverOrder(Long orderId) {
        log.info("deliverOrder started for orderId={}", orderId);
        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.SHIPPED) {
            log.warn("deliverOrder invalid status orderId={} status={}", orderId, order.getStatus());
            throw new IllegalStateException("Only SHIPPED orders can be delivered");
        }

        order.setStatus(OrderStatus.DELIVERED);
        Order savedOrder = orderRepository.save(order);
        log.info("deliverOrder completed orderId={}", orderId);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDto cancelOrder(Long orderId) {
        log.info("cancelOrder started for orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        switch (order.getStatus()) {

            case CREATED:
                // No payment → no refund
                order.setStatus(OrderStatus.CANCELLED);
                log.info("cancelOrder CREATED->CANCELLED orderId={}", orderId);
                break;

            case PAID:
            case SHIPPED:
                // Payment exists → refund required
                log.info("cancelOrder processing refund orderId={}", orderId);
                paymentService.refundPayment(order.getId());
                order.setStatus(OrderStatus.REFUNDED);
                break;

            case DELIVERED:
                log.warn("cancelOrder cannot cancel delivered order orderId={}", orderId);
                throw new IllegalStateException("Delivered order cannot be cancelled");

            default:
                log.error("cancelOrder invalid order state orderId={} status={}", orderId, order.getStatus());
                throw new IllegalStateException("Invalid order state");
        }

        Order savedOrder = orderRepository.save(order);
        log.info("cancelOrder completed orderId={}", orderId);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public OrderResponseDto placeOrderForCurrentUser() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return placeOrder(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersForCurrentUser() {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return getOrdersByUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<OrderResponseDto> getOrdersForCurrentUser(Pageable pageable) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return getOrdersByUser(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        
        // Check ownership: Admin can see all orders, users can only see their own
        String currentUsername = securityUtils.getCurrentUsername();
        if (currentUsername == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
        
        // Admin can access any order
        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            return orderMapper.toDto(order);
        }
        
        // Regular users can only access their own orders
        Long currentUserId = securityUtils.getCurrentUserId();
        if (order.getUser() == null || !order.getUser().getId().equals(currentUserId)) {
            log.warn("Access denied: User {} attempted to access order {} owned by user {}", 
                    currentUserId, orderId, order.getUser() != null ? order.getUser().getId() : "null");
            throw new AccessDeniedException("You do not have permission to access this order");
        }
        
        return orderMapper.toDto(order);
    }

    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

}

