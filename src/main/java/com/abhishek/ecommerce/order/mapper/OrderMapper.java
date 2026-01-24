package com.abhishek.ecommerce.order.mapper;

import com.abhishek.ecommerce.order.dto.response.OrderItemResponseDto;
import com.abhishek.ecommerce.order.dto.response.OrderResponseDto;
import com.abhishek.ecommerce.order.entity.Order;
import com.abhishek.ecommerce.order.entity.OrderItem;
import com.abhishek.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final PaymentRepository paymentRepository;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderMapper.class);

    // ================= RESPONSE =================
    public OrderResponseDto toDto(Order order) {
        if (order == null) {
            return null;
        }

        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUser() != null ? order.getUser().getId() : null);
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        dto.setTotalAmount(order.getTotalAmount() != null ? order.getTotalAmount().getAmount() : null);
        dto.setCurrency(order.getTotalAmount() != null ? order.getTotalAmount().getCurrency() : null);
        dto.setCreatedAt(order.getCreatedAt());
        dto.setItems(itemsToDto(order.getItems()));
        
        // Fetch payment info
        log.info("[OrderMapper] Fetching payment for orderId={}", order.getId());
        var paymentOpt = paymentRepository.findByOrderId(order.getId());
        if (paymentOpt.isPresent()) {
            var payment = paymentOpt.get();
            log.info("[OrderMapper] Payment found: id={}, method={}, status={}", 
                    payment.getId(), payment.getPaymentMethod(), payment.getStatus());
            dto.setPaymentStatus(payment.getStatus() != null ? payment.getStatus().name() : null);
            dto.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null);
            log.info("[OrderMapper] DTO paymentMethod set to: {}", dto.getPaymentMethod());
        } else {
            log.warn("[OrderMapper] No payment found for orderId={}", order.getId());
        }

        log.info("[OrderMapper] Returning DTO for orderId={} with paymentMethod={}, paymentStatus={}", 
                order.getId(), dto.getPaymentMethod(), dto.getPaymentStatus());
        return dto;
    }

    public OrderItemResponseDto itemToDto(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        OrderItemResponseDto dto = new OrderItemResponseDto();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct() != null ? orderItem.getProduct().getId() : null);
        dto.setProductName(orderItem.getProduct() != null ? orderItem.getProduct().getName() : null);
        dto.setQuantity(orderItem.getQuantity());
        dto.setPriceAmount(orderItem.getPrice() != null ? orderItem.getPrice().getAmount() : null);
        dto.setCurrency(orderItem.getPrice() != null ? orderItem.getPrice().getCurrency() : null);

        return dto;
    }

    public List<OrderItemResponseDto> itemsToDto(List<OrderItem> items) {
        if (items == null) {
            return null;
        }

        return items.stream()
                .map(this::itemToDto)
                .collect(Collectors.toList());
    }
}

