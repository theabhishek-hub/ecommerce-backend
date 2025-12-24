package com.abhishek.ecommerce.inventory.service;

public interface InventoryService {

    void addStock(Long productId, int quantity);

    void reduceStock(Long productId, int quantity);

    int getAvailableStock(Long productId);
}

