package com.abhishek.ecommerce.cart.mapper;

import com.abhishek.ecommerce.cart.dto.response.CartItemResponseDto;
import com.abhishek.ecommerce.cart.dto.response.CartResponseDto;
import com.abhishek.ecommerce.cart.entity.Cart;
import com.abhishek.ecommerce.cart.entity.CartItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    // ================= RESPONSE =================
    @Mapping(target = "userId", expression = "java(cart.getUser() != null ? cart.getUser().getId() : null)")
    @Mapping(target = "items", source = "items")
    CartResponseDto toDto(Cart cart);

    @Mapping(target = "productId", expression = "java(cartItem.getProduct() != null ? cartItem.getProduct().getId() : null)")
    @Mapping(target = "productName", expression = "java(cartItem.getProduct() != null ? cartItem.getProduct().getName() : null)")
    @Mapping(target = "priceAmount", expression = "java(cartItem.getPrice() != null ? cartItem.getPrice().getAmount() : null)")
    @Mapping(target = "currency", expression = "java(cartItem.getPrice() != null ? cartItem.getPrice().getCurrency() : null)")
    CartItemResponseDto itemToDto(CartItem cartItem);

    List<CartItemResponseDto> itemsToDto(List<CartItem> items);
}

