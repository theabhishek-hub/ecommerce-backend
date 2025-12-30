package com.abhishek.ecommerce.inventory.service;

import com.abhishek.ecommerce.inventory.dto.request.UpdateStockRequestDto;
import com.abhishek.ecommerce.inventory.dto.response.InventoryResponseDto;

public interface InventoryService {

    InventoryResponseDto increaseStock(Long productId, UpdateStockRequestDto requestDto);

    InventoryResponseDto reduceStock(Long productId, UpdateStockRequestDto requestDto);

    InventoryResponseDto getAvailableStock(Long productId);
}

