package com.abhishek.ecommerce.inventory.service;

import com.abhishek.ecommerce.inventory.dto.request.UpdateStockRequestDto;
import com.abhishek.ecommerce.inventory.dto.response.InventoryResponseDto;
import com.abhishek.ecommerce.inventory.entity.Inventory;
import com.abhishek.ecommerce.inventory.exception.InsufficientStockException;
import com.abhishek.ecommerce.inventory.exception.InventoryNotFoundException;
import com.abhishek.ecommerce.inventory.mapper.InventoryMapper;
import com.abhishek.ecommerce.inventory.repository.InventoryRepository;
import com.abhishek.ecommerce.inventory.service.impl.InventoryServiceImpl;
import com.abhishek.ecommerce.product.entity.Product;
import com.abhishek.ecommerce.product.exception.ProductNotFoundException;
import com.abhishek.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Inventory inventory;
    private Product product;
    private InventoryResponseDto inventoryResponseDto;
    private UpdateStockRequestDto updateStockRequestDto;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");

        inventory = new Inventory();
        inventory.setId(1L);
        inventory.setProduct(product);
        inventory.setQuantity(10);
        inventory.setVersion(1L);

        inventoryResponseDto = new InventoryResponseDto();
        inventoryResponseDto.setId(1L);
        inventoryResponseDto.setProductId(1L);
        inventoryResponseDto.setProductName("Test Product");
        inventoryResponseDto.setQuantity(10);

        updateStockRequestDto = new UpdateStockRequestDto();
        updateStockRequestDto.setQuantity(5);
    }

    @Test
    void increaseStock_ShouldIncreaseStockSuccessfully() {
        // Given
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMapper.toDto(inventory)).thenReturn(inventoryResponseDto);

        // When
        InventoryResponseDto result = inventoryService.increaseStock(1L, updateStockRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(10);
        assertThat(inventory.getQuantity()).isEqualTo(15); // 10 + 5

        verify(inventoryRepository).findByProductId(1L);
        verify(inventoryRepository).save(any(Inventory.class));
        verify(inventoryMapper).toDto(inventory);
    }

    @Test
    void increaseStock_ShouldCreateNewInventory_WhenInventoryDoesNotExist() {
        // Given
        Inventory newInventory = new Inventory();
        newInventory.setId(2L);
        newInventory.setProduct(product);
        newInventory.setQuantity(5); // Will be set by service
        newInventory.setVersion(1L);

        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(newInventory);
        when(inventoryMapper.toDto(newInventory)).thenReturn(inventoryResponseDto);

        // When
        InventoryResponseDto result = inventoryService.increaseStock(1L, updateStockRequestDto);

        // Then
        assertThat(result).isNotNull();
        // The service creates inventory with quantity 0, then adds 5, so final quantity should be 5

        verify(inventoryRepository).findByProductId(1L);
        verify(productRepository).findById(1L);
        verify(inventoryRepository).save(any(Inventory.class)); // Only one save call
        verify(inventoryMapper).toDto(newInventory);
    }

    @Test
    void increaseStock_ShouldThrowException_WhenProductNotFound() {
        // Given
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryService.increaseStock(1L, updateStockRequestDto))
                .isInstanceOf(ProductNotFoundException.class);

        verify(inventoryRepository).findByProductId(1L);
        verify(productRepository).findById(1L);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void increaseStock_ShouldRetryOnOptimisticLockingFailure() {
        // Given
        when(inventoryRepository.findByProductId(1L))
                .thenReturn(Optional.of(inventory))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class)))
                .thenThrow(ObjectOptimisticLockingFailureException.class)
                .thenReturn(inventory);
        when(inventoryMapper.toDto(inventory)).thenReturn(inventoryResponseDto);

        // When
        InventoryResponseDto result = inventoryService.increaseStock(1L, updateStockRequestDto);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository, times(2)).findByProductId(1L);
        verify(inventoryRepository, times(2)).save(any(Inventory.class));
        verify(inventoryMapper).toDto(inventory);
    }

    @Test
    void increaseStock_ShouldThrowException_AfterMaxRetries() {
        // Given
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class)))
                .thenThrow(ObjectOptimisticLockingFailureException.class);

        // When & Then
        assertThatThrownBy(() -> inventoryService.increaseStock(1L, updateStockRequestDto))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        verify(inventoryRepository, times(3)).findByProductId(1L);
        verify(inventoryRepository, times(3)).save(any(Inventory.class));
    }

    @Test
    void reduceStock_ShouldReduceStockSuccessfully() {
        // Given
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(inventoryMapper.toDto(inventory)).thenReturn(inventoryResponseDto);

        // When
        InventoryResponseDto result = inventoryService.reduceStock(1L, updateStockRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(inventory.getQuantity()).isEqualTo(5); // 10 - 5

        verify(inventoryRepository).findByProductId(1L);
        verify(inventoryRepository).save(any(Inventory.class));
        verify(inventoryMapper).toDto(inventory);
    }

    @Test
    void reduceStock_ShouldThrowException_WhenInventoryNotFound() {
        // Given
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryService.reduceStock(1L, updateStockRequestDto))
                .isInstanceOf(InventoryNotFoundException.class);

        verify(inventoryRepository).findByProductId(1L);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void reduceStock_ShouldThrowException_WhenInsufficientStock() {
        // Given
        updateStockRequestDto.setQuantity(15); // More than available (10)
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        // When & Then
        assertThatThrownBy(() -> inventoryService.reduceStock(1L, updateStockRequestDto))
                .isInstanceOf(InsufficientStockException.class);

        verify(inventoryRepository).findByProductId(1L);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void reduceStock_ShouldRetryOnOptimisticLockingFailure() {
        // Given
        when(inventoryRepository.findByProductId(1L))
                .thenReturn(Optional.of(inventory))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class)))
                .thenThrow(ObjectOptimisticLockingFailureException.class)
                .thenReturn(inventory);
        when(inventoryMapper.toDto(inventory)).thenReturn(inventoryResponseDto);

        // When
        InventoryResponseDto result = inventoryService.reduceStock(1L, updateStockRequestDto);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository, times(2)).findByProductId(1L);
        verify(inventoryRepository, times(2)).save(any(Inventory.class));
        verify(inventoryMapper).toDto(inventory);
    }

    @Test
    void getAvailableStock_ShouldReturnExistingInventory() {
        // Given
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryMapper.toDto(inventory)).thenReturn(inventoryResponseDto);

        // When
        InventoryResponseDto result = inventoryService.getAvailableStock(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(10);

        verify(inventoryRepository).findByProductId(1L);
        verify(inventoryMapper).toDto(inventory);
    }

    @Test
    void getAvailableStock_ShouldReturnEmptyInventory_WhenInventoryDoesNotExist() {
        // Given
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryMapper.toDto(any(Inventory.class))).thenReturn(inventoryResponseDto);

        // When
        InventoryResponseDto result = inventoryService.getAvailableStock(1L);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryRepository).findByProductId(1L);
        verify(productRepository).findById(1L);
        verify(inventoryMapper).toDto(any(Inventory.class));
    }

    @Test
    void getAvailableStock_ShouldThrowException_WhenProductNotFound() {
        // Given
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryService.getAvailableStock(1L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(inventoryRepository).findByProductId(1L);
        verify(productRepository).findById(1L);
        verify(inventoryMapper, never()).toDto(any(Inventory.class));
    }
}