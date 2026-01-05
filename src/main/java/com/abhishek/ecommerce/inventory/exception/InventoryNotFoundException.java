package com.abhishek.ecommerce.inventory.exception;

import com.abhishek.ecommerce.common.exception.BusinessException;

public class InventoryNotFoundException extends BusinessException {

    public InventoryNotFoundException(Long id) {
        super("Inventory not found with id: " + id, "INVENTORY_NOT_FOUND");
    }
}

