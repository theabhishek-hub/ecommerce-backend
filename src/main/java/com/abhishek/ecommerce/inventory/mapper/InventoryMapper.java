package com.abhishek.ecommerce.inventory.mapper;

import com.abhishek.ecommerce.inventory.dto.response.InventoryResponseDto;
import com.abhishek.ecommerce.inventory.entity.Inventory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    // ================= RESPONSE =================
    @Mapping(target = "productId", expression = "java(inventory.getProduct() != null ? inventory.getProduct().getId() : null)")
    @Mapping(target = "productName", expression = "java(inventory.getProduct() != null ? inventory.getProduct().getName() : null)")
    InventoryResponseDto toDto(Inventory inventory);
}

