package com.abhishek.ecommerce.order.mapper;

import com.abhishek.ecommerce.order.dto.response.OrderItemResponseDto;
import com.abhishek.ecommerce.order.dto.response.OrderResponseDto;
import com.abhishek.ecommerce.order.entity.Order;
import com.abhishek.ecommerce.order.entity.OrderItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // ================= RESPONSE =================
    @Mapping(target = "userId", expression = "java(order.getUser() != null ? order.getUser().getId() : null)")
    @Mapping(target = "status", expression = "java(order.getStatus() != null ? order.getStatus().name() : null)")
    @Mapping(target = "totalAmount", expression = "java(order.getTotalAmount() != null ? order.getTotalAmount().getAmount() : null)")
    @Mapping(target = "currency", expression = "java(order.getTotalAmount() != null ? order.getTotalAmount().getCurrency() : null)")
    @Mapping(target = "items", source = "items")
    OrderResponseDto toDto(Order order);

    @Mapping(target = "productId", expression = "java(orderItem.getProduct() != null ? orderItem.getProduct().getId() : null)")
    @Mapping(target = "productName", expression = "java(orderItem.getProduct() != null ? orderItem.getProduct().getName() : null)")
    @Mapping(target = "priceAmount", expression = "java(orderItem.getPrice() != null ? orderItem.getPrice().getAmount() : null)")
    @Mapping(target = "currency", expression = "java(orderItem.getPrice() != null ? orderItem.getPrice().getCurrency() : null)")
    OrderItemResponseDto itemToDto(OrderItem orderItem);

    List<OrderItemResponseDto> itemsToDto(List<OrderItem> items);
}

