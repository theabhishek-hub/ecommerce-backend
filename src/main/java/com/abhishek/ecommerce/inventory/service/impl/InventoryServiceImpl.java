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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Inventory business logic
 */
@Slf4j
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
        log.info("increaseStock started for productId={} qty={}", productId, requestDto.getQuantity());

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> {
                    log.info("increaseStock creating new inventory for productId={}", productId);
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

                    Inventory inv = new Inventory();
                    inv.setProduct(product);
                    inv.setQuantity(0);
                    return inv;
                });

        inventory.setQuantity(inventory.getQuantity() + requestDto.getQuantity());
        Inventory savedInventory = inventoryRepository.save(inventory);
        log.info("increaseStock completed productId={} newQty={}", productId, savedInventory.getQuantity());
        return inventoryMapper.toDto(savedInventory);
    }

    // ========================= REDUCE STOCK =========================
    @Override
    public InventoryResponseDto reduceStock(Long productId, UpdateStockRequestDto requestDto) {
        log.info("reduceStock started for productId={} qty={}", productId, requestDto.getQuantity());

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product id: " + productId));

        if (inventory.getQuantity() < requestDto.getQuantity()) {
            log.warn("reduceStock insufficient stock productId={} available={} requested={}",
                    productId, inventory.getQuantity(), requestDto.getQuantity());
            throw new InsufficientStockException(
                    "Insufficient stock. Available: " + inventory.getQuantity() + ", Requested: " + requestDto.getQuantity()
            );
        }

        inventory.setQuantity(inventory.getQuantity() - requestDto.getQuantity());
        Inventory savedInventory = inventoryRepository.save(inventory);
        log.info("reduceStock completed productId={} newQty={}", productId, savedInventory.getQuantity());
        return inventoryMapper.toDto(savedInventory);
    }

    // ========================= GET STOCK =========================
    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDto getAvailableStock(Long productId) {
        log.debug("getAvailableStock for productId={}", productId);
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> {
                    // Return empty inventory if not found
                    log.debug("getAvailableStock no inventory found for productId={}", productId);
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

