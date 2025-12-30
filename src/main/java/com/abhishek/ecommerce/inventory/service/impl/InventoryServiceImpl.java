package com.abhishek.ecommerce.inventory.service.impl;

import com.abhishek.ecommerce.inventory.dto.request.UpdateStockRequestDto;
import com.abhishek.ecommerce.inventory.dto.response.InventoryResponseDto;
import com.abhishek.ecommerce.inventory.entity.Inventory;
import com.abhishek.ecommerce.inventory.exception.InsufficientStockException;
import com.abhishek.ecommerce.inventory.exception.InventoryNotFoundException;
import com.abhishek.ecommerce.inventory.mapper.InventoryMapper;
import com.abhishek.ecommerce.inventory.repository.InventoryRepository;
import com.abhishek.ecommerce.inventory.service.InventoryService;
import com.abhishek.ecommerce.product.entity.Product;
import com.abhishek.ecommerce.product.exception.ProductNotFoundException;
import com.abhishek.ecommerce.product.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Inventory business logic
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper inventoryMapper;

    // ========================= INCREASE STOCK =========================
    @Override
    public InventoryResponseDto increaseStock(Long productId, UpdateStockRequestDto requestDto) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

                    Inventory inv = new Inventory();
                    inv.setProduct(product);
                    inv.setQuantity(0);
                    return inv;
                });

        inventory.setQuantity(inventory.getQuantity() + requestDto.getQuantity());
        Inventory savedInventory = inventoryRepository.save(inventory);
        return inventoryMapper.toDto(savedInventory);
    }

    // ========================= REDUCE STOCK =========================
    @Override
    public InventoryResponseDto reduceStock(Long productId, UpdateStockRequestDto requestDto) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product id: " + productId));

        if (inventory.getQuantity() < requestDto.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock. Available: " + inventory.getQuantity() + ", Requested: " + requestDto.getQuantity()
            );
        }

        inventory.setQuantity(inventory.getQuantity() - requestDto.getQuantity());
        Inventory savedInventory = inventoryRepository.save(inventory);
        return inventoryMapper.toDto(savedInventory);
    }

    // ========================= GET STOCK =========================
    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDto getAvailableStock(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> {
                    // Return empty inventory if not found
                    Inventory inv = new Inventory();
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
                    inv.setProduct(product);
                    inv.setQuantity(0);
                    return inv;
                });
        return inventoryMapper.toDto(inventory);
    }
}


