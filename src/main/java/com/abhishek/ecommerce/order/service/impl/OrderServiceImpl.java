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
import com.abhishek.ecommerce.payment.entity.PaymentMethod;
import com.abhishek.ecommerce.payment.service.PaymentService;
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
    private final PaymentService paymentService;


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

        // Create payment entry (COD for now)
        paymentService.createPayment(
                savedOrder.getId(),
                PaymentMethod.COD
        );


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
    public Order shipOrder(Long orderId) {
        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Only PAID orders can be shipped");
        }

        order.setStatus(OrderStatus.SHIPPED);
        return orderRepository.save(order);
    }

    @Override
    public Order deliverOrder(Long orderId) {
        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Only SHIPPED orders can be delivered");
        }

        order.setStatus(OrderStatus.DELIVERED);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        switch (order.getStatus()) {

            case CREATED:
                // No payment → no refund
                order.setStatus(OrderStatus.CANCELLED);
                break;

            case PAID:
            case SHIPPED:
                // Payment exists → refund required
                paymentService.refundPayment(order.getId());
                order.setStatus(OrderStatus.REFUNDED);
                break;

            case DELIVERED:
                throw new RuntimeException("Delivered order cannot be cancelled");

            default:
                throw new RuntimeException("Invalid order state");
        }

        return orderRepository.save(order);
    }


    @Override
    public Order getOrderById(Long orderId) {
        return getOrderOrThrow(orderId);
    }

    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

}


