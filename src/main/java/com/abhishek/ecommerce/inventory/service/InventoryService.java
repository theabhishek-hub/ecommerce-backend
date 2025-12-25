package com.abhishek.ecommerce.inventory.service;

public interface InventoryService {

    void increaseStock(Long productId, int quantity);

    void reduceStock(Long productId, int quantity);

    int getAvailableStock(Long productId);
}

