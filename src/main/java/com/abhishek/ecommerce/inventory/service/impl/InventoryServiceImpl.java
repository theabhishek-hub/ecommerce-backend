package com.abhishek.ecommerce.inventory.service.impl;

import com.abhishek.ecommerce.common.apiResponse.PageResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

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

        int maxRetries = 3;
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                Inventory inventory = inventoryRepository.findByProductId(productId)
                        .orElseGet(() -> {
                            log.info("increaseStock creating new inventory for productId={}", productId);
                            Product product = productRepository.findById(productId)
                                    .orElseThrow(() -> new ProductNotFoundException(productId));

                            Inventory inv = new Inventory();
                            inv.setProduct(product);
                            inv.setQuantity(0);
                            return inv;
                        });

                inventory.setQuantity(inventory.getQuantity() + requestDto.getQuantity());
                Inventory savedInventory = inventoryRepository.save(inventory);
                log.info("increaseStock completed productId={} newQty={}", productId, savedInventory.getQuantity());
                return inventoryMapper.toDto(savedInventory);
            } catch (ObjectOptimisticLockingFailureException e) {
                attempt++;
                log.warn("Optimistic locking failure in increaseStock for productId={}, attempt {}/{}", productId, attempt, maxRetries);
                if (attempt >= maxRetries) {
                    log.error("Failed to increase stock after {} attempts for productId={}", maxRetries, productId);
                    throw e;
                }
            }
        }
        throw new RuntimeException("Unexpected error in increaseStock");
    }

    // ========================= REDUCE STOCK =========================
    @Override
    public InventoryResponseDto reduceStock(Long productId, UpdateStockRequestDto requestDto) {
        log.info("reduceStock started for productId={} qty={}", productId, requestDto.getQuantity());

        int maxRetries = 3;
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                Inventory inventory = inventoryRepository.findByProductId(productId)
                        .orElseThrow(() -> new InventoryNotFoundException(productId));

                if (inventory.getQuantity() < requestDto.getQuantity()) {
                    log.warn("reduceStock insufficient stock productId={} available={} requested={}",
                            productId, inventory.getQuantity(), requestDto.getQuantity());
                    throw new InsufficientStockException(productId, requestDto.getQuantity(), inventory.getQuantity());
                }

                inventory.setQuantity(inventory.getQuantity() - requestDto.getQuantity());
                Inventory savedInventory = inventoryRepository.save(inventory);
                log.info("reduceStock completed productId={} newQty={}", productId, savedInventory.getQuantity());
                return inventoryMapper.toDto(savedInventory);
            } catch (ObjectOptimisticLockingFailureException e) {
                attempt++;
                log.warn("Optimistic locking failure in reduceStock for productId={}, attempt {}/{}", productId, attempt, maxRetries);
                if (attempt >= maxRetries) {
                    log.error("Failed to reduce stock after {} attempts for productId={}", maxRetries, productId);
                    throw e;
                }
            }
        }
        throw new RuntimeException("Unexpected error in reduceStock");
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
                            .orElseThrow(() -> new ProductNotFoundException(productId));
                    inv.setProduct(product);
                    inv.setQuantity(0);
                    return inv;
                });
        return inventoryMapper.toDto(inventory);
    }

    // ========================= GET INVENTORY BY SELLER =========================
    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<InventoryResponseDto> getInventoryBySeller(Long sellerId, Pageable pageable) {
        log.debug("getInventoryBySeller for sellerId={}", sellerId);
        Page<Inventory> inventoryPage = inventoryRepository.findBySellerId(sellerId, pageable);
        
        // Force initialization of lazy relationships within transaction
        inventoryPage.getContent().forEach(inventory -> {
            if (inventory.getProduct() != null) {
                inventory.getProduct().getName(); // Initialize product
                if (inventory.getProduct().getSeller() != null) {
                    inventory.getProduct().getSeller().getId(); // Initialize seller
                    if (inventory.getProduct().getSeller().getUser() != null) {
                        inventory.getProduct().getSeller().getUser().getFullName(); // Initialize user
                    }
                }
            }
        });
        
        return mapToPageResponseDto(inventoryPage);
    }

    // ========================= GET INVENTORY BY SELLER WITH SEARCH =========================
    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<InventoryResponseDto> getInventoryBySellerAndSearch(Long sellerId, String searchQuery, Pageable pageable) {
        log.debug("getInventoryBySellerAndSearch for sellerId={}, searchQuery={}", sellerId, searchQuery);
        String search = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim() : "";
        Page<Inventory> inventoryPage = inventoryRepository.findBySellerIdAndProductNameContaining(sellerId, search, pageable);
        
        // Force initialization of lazy relationships within transaction
        inventoryPage.getContent().forEach(inventory -> {
            if (inventory.getProduct() != null) {
                inventory.getProduct().getName(); // Initialize product
                if (inventory.getProduct().getSeller() != null) {
                    inventory.getProduct().getSeller().getId(); // Initialize seller
                    if (inventory.getProduct().getSeller().getUser() != null) {
                        inventory.getProduct().getSeller().getUser().getFullName(); // Initialize user
                    }
                }
            }
        });
        
        return mapToPageResponseDto(inventoryPage);
    }

    // ========================= GET ALL INVENTORY (ADMIN) =========================
    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<InventoryResponseDto> getAllInventory(Pageable pageable) {
        log.debug("getAllInventory - admin view");
        // Use native query with JOIN FETCH for admin view to avoid N+1 queries
        Page<Inventory> inventoryPage = inventoryRepository.findAll(pageable);
        
        // Manually fetch seller and user relationships for each inventory item
        inventoryPage.getContent().forEach(inventory -> {
            if (inventory.getProduct() != null && inventory.getProduct().getSeller() != null) {
                // Force initialization of lazy relationships
                inventory.getProduct().getSeller().getId();
                if (inventory.getProduct().getSeller().getUser() != null) {
                    inventory.getProduct().getSeller().getUser().getFullName();
                }
            }
        });
        
        return mapToPageResponseDto(inventoryPage);
    }

    private PageResponseDto<InventoryResponseDto> mapToPageResponseDto(Page<Inventory> inventoryPage) {
        return PageResponseDto.<InventoryResponseDto>builder()
                .content(inventoryPage.getContent().stream()
                        .map(inventoryMapper::toDto)
                        .collect(Collectors.toList()))
                .pageNumber(inventoryPage.getNumber())
                .pageSize(inventoryPage.getSize())
                .totalElements(inventoryPage.getTotalElements())
                .totalPages(inventoryPage.getTotalPages())
                .first(inventoryPage.isFirst())
                .last(inventoryPage.isLast())
                .empty(inventoryPage.isEmpty())
                .build();
    }
}

