package com.abhishek.ecommerce.order.service.impl;

import com.abhishek.ecommerce.cart.entity.Cart;
import com.abhishek.ecommerce.cart.entity.CartItem;
import com.abhishek.ecommerce.cart.repository.CartRepository;
import com.abhishek.ecommerce.inventory.service.InventoryService;
import com.abhishek.ecommerce.order.entity.Order;
import com.abhishek.ecommerce.order.entity.OrderItem;
import com.abhishek.ecommerce.order.entity.OrderStatus;
import com.abhishek.ecommerce.order.repository.OrderRepository;
import com.abhishek.ecommerce.order.service.OrderService;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.abhishek.ecommerce.common.entity.Money;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional   // 🔥 VERY IMPORTANT
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final InventoryService inventoryService;

    @Override
    public Order placeOrder(Long userId) {

        // 1️⃣ Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2️⃣ Fetch cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
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
            inventoryService.reduceStock(
                    cartItem.getProduct().getId(),
                    cartItem.getQuantity()
            );

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

        // 7️⃣ Clear cart SAFELY
        cart.getItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    @Override
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}


