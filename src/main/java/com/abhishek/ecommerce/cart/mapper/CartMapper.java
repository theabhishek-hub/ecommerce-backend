package com.abhishek.ecommerce.cart.mapper;

import com.abhishek.ecommerce.cart.dto.response.CartItemResponseDto;
import com.abhishek.ecommerce.cart.dto.response.CartResponseDto;
import com.abhishek.ecommerce.cart.entity.Cart;
import com.abhishek.ecommerce.cart.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    // ================= RESPONSE =================
    public CartResponseDto toDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        CartResponseDto dto = new CartResponseDto();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser() != null ? cart.getUser().getId() : null);
        dto.setItems(itemsToDto(cart.getItems()));

        return dto;
    }

    public CartItemResponseDto itemToDto(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        CartItemResponseDto dto = new CartItemResponseDto();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct() != null ? cartItem.getProduct().getId() : null);
        dto.setProductName(cartItem.getProduct() != null ? cartItem.getProduct().getName() : null);
        dto.setQuantity(cartItem.getQuantity());
        dto.setPriceAmount(cartItem.getPrice() != null ? cartItem.getPrice().getAmount() : null);
        dto.setCurrency(cartItem.getPrice() != null ? cartItem.getPrice().getCurrency() : null);
        dto.setImageUrl(cartItem.getProduct() != null ? cartItem.getProduct().getImageUrl() : null);

        return dto;
    }

    public List<CartItemResponseDto> itemsToDto(List<CartItem> items) {
        if (items == null) {
            return null;
        }

        return items.stream()
                .map(this::itemToDto)
                .collect(Collectors.toList());
    }
}

