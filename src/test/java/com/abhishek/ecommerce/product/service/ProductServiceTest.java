package com.abhishek.ecommerce.product.service;

import com.abhishek.ecommerce.product.service.impl.ProductServiceImpl;

import com.abhishek.ecommerce.common.entity.Money;
import com.abhishek.ecommerce.product.dto.request.ProductCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.ProductUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;
import com.abhishek.ecommerce.product.entity.*;
import com.abhishek.ecommerce.product.exception.ProductNotFoundException;
import com.abhishek.ecommerce.product.mapper.ProductMapper;
import com.abhishek.ecommerce.product.repository.BrandRepository;
import com.abhishek.ecommerce.product.repository.CategoryRepository;
import com.abhishek.ecommerce.product.repository.ProductRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductResponseDto productResponseDto;
    private ProductCreateRequestDto createRequestDto;
    private ProductUpdateRequestDto updateRequestDto;
    private Category category;
    private Brand brand;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setStatus(CategoryStatus.ACTIVE);

        brand = new Brand();
        brand.setId(1L);
        brand.setName("Samsung");
        brand.setStatus(BrandStatus.ACTIVE);

        product = new Product();
        product.setId(1L);
        product.setSku("TEST-SKU-001");
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new Money(new BigDecimal("99.99"), "USD"));
        product.setCategory(category);
        product.setBrand(brand);
        product.setStatus(ProductStatus.ACTIVE);

        productResponseDto = new ProductResponseDto();
        productResponseDto.setId(1L);
        productResponseDto.setSku("TEST-SKU-001");
        productResponseDto.setName("Test Product");
        productResponseDto.setDescription("Test Description");
        productResponseDto.setPriceAmount(new BigDecimal("99.99"));
        productResponseDto.setCurrency("USD");
        productResponseDto.setCategoryName("Electronics");
        productResponseDto.setBrandName("Samsung");
        productResponseDto.setStatus("ACTIVE");

        createRequestDto = new ProductCreateRequestDto();
        createRequestDto.setSku("TEST-SKU-001");
        createRequestDto.setName("Test Product");
        createRequestDto.setDescription("Test Description");
        createRequestDto.setPriceAmount(new BigDecimal("99.99"));
        createRequestDto.setCurrency("USD");
        createRequestDto.setCategoryId(1L);
        createRequestDto.setBrandId(1L);

        updateRequestDto = new ProductUpdateRequestDto();
        updateRequestDto.setName("Updated Product");
        updateRequestDto.setPriceAmount(new BigDecimal("149.99"));
        updateRequestDto.setCurrency("USD");
    }

    @Test
    void createProduct_ShouldCreateProductSuccessfully() {
        // Given
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(productMapper.toEntity(createRequestDto)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(productResponseDto);

        // When
        ProductResponseDto result = productService.createProduct(createRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("TEST-SKU-001");
        assertThat(result.getName()).isEqualTo("Test Product");

        verify(productRepository).existsBySku("TEST-SKU-001");
        verify(categoryRepository).findById(1L);
        verify(brandRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getProductById_ShouldReturnProduct() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(productResponseDto);

        // When
        ProductResponseDto result = productService.getProductById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSku()).isEqualTo("TEST-SKU-001");

        verify(productRepository).findById(1L);
        verify(productMapper).toDto(product);
    }

    @Test
    void getProductById_ShouldThrowExceptionWhenNotFound() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProductById(1L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findById(1L);
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Given
        List<Product> products = List.of(product);
        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toDto(product)).thenReturn(productResponseDto);

        // When
        List<ProductResponseDto> result = productService.getAllProducts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getSku()).isEqualTo("TEST-SKU-001");

        verify(productRepository).findAll();
    }

    @Test
    void updateProduct_ShouldUpdateSuccessfully() {
        // Given
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setSku("TEST-SKU-001");
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Test Description");
        updatedProduct.setPrice(new Money(new BigDecimal("149.99"), "USD"));
        updatedProduct.setCategory(category);
        updatedProduct.setBrand(brand);
        updatedProduct.setStatus(ProductStatus.ACTIVE);

        ProductResponseDto updatedResponseDto = getResponseDto();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productMapper.toDto(updatedProduct)).thenReturn(updatedResponseDto);

        // When
        ProductResponseDto result = productService.updateProduct(1L, updateRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Product");
        assertThat(result.getPriceAmount()).isEqualByComparingTo(new BigDecimal("149.99"));

        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    private static @NonNull ProductResponseDto getResponseDto() {
        ProductResponseDto updatedResponseDto = new ProductResponseDto();
        updatedResponseDto.setId(1L);
        updatedResponseDto.setSku("TEST-SKU-001");
        updatedResponseDto.setName("Updated Product");
        updatedResponseDto.setDescription("Test Description");
        updatedResponseDto.setPriceAmount(new BigDecimal("149.99"));
        updatedResponseDto.setCurrency("USD");
        updatedResponseDto.setCategoryName("Electronics");
        updatedResponseDto.setBrandName("Samsung");
        updatedResponseDto.setStatus("ACTIVE");
        return updatedResponseDto;
    }

    @Test
    void deleteProduct_ShouldMarkAsInactive() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.deleteProduct(1L);

        // Then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
    }

    @Test
    void activateProduct_ShouldSetStatusToActive() {
        // Given
        product.setStatus(ProductStatus.INACTIVE);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.activateProduct(1L);

        // Then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
    }

    @Test
    void deactivateProduct_ShouldSetStatusToInactive() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.deactivateProduct(1L);

        // Then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
    }
}