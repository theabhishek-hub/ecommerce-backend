package com.abhishek.ecommerce.inventory.mapper;

import com.abhishek.ecommerce.inventory.dto.response.InventoryResponseDto;
import com.abhishek.ecommerce.inventory.entity.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    // ================= RESPONSE =================
    public InventoryResponseDto toDto(Inventory inventory) {
        if (inventory == null) {
            return null;
        }

        InventoryResponseDto dto = new InventoryResponseDto();
        dto.setId(inventory.getId());
        dto.setProductId(inventory.getProduct() != null ? inventory.getProduct().getId() : null);
        dto.setProductName(inventory.getProduct() != null ? inventory.getProduct().getName() : null);
        dto.setSku(inventory.getProduct() != null ? inventory.getProduct().getSku() : null);
        
        // Seller info
        if (inventory.getProduct() != null && inventory.getProduct().getSeller() != null) {
            dto.setSellerId(inventory.getProduct().getSeller().getId());
            dto.setSellerName(inventory.getProduct().getSeller().getFullName());
        }
        
        dto.setQuantity(inventory.getQuantity());

        return dto;
    }
}

