package com.abhishek.ecommerce.product.service;

import com.abhishek.ecommerce.product.service.ProductService;
import com.abhishek.ecommerce.product.service.impl.ProductServiceImpl;

import com.abhishek.ecommerce.common.api.PageResponseDto;
import com.abhishek.ecommerce.product.dto.request.ProductCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.ProductUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;
import com.abhishek.ecommerce.product.entity.*;
import com.abhishek.ecommerce.product.exception.ProductNotFoundException;
import com.abhishek.ecommerce.product.mapper.ProductMapper;
import com.abhishek.ecommerce.product.repository.BrandRepository;
import com.abhishek.ecommerce.product.repository.CategoryRepository;
import com.abhishek.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
        category = Category.builder()
                .id(1L)
                .name("Electronics")
                .status(CategoryStatus.ACTIVE)
                .build();

        brand = Brand.builder()
                .id(1L)
                .name("Samsung")
                .status(BrandStatus.ACTIVE)
                .build();

        product = Product.builder()
                .id(1L)
                .sku("TEST-SKU-001")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .category(category)
                .brand(brand)
                .status(ProductStatus.ACTIVE)
                .build();

        productResponseDto = ProductResponseDto.builder()
                .id(1L)
                .sku("TEST-SKU-001")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .categoryName("Electronics")
                .brandName("Samsung")
                .status(ProductStatus.ACTIVE)
                .build();

        createRequestDto = ProductCreateRequestDto.builder()
                .sku("TEST-SKU-001")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .categoryId(1L)
                .brandId(1L)
                .build();

        updateRequestDto = ProductUpdateRequestDto.builder()
                .name("Updated Product")
                .price(new BigDecimal("149.99"))
                .build();
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
        assertThat(result.get(0).getSku()).isEqualTo("TEST-SKU-001");

        verify(productRepository).findAll();
    }

    @Test
    void getAllProducts_WithPageable_ShouldReturnPagedResult() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);
        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productMapper.toDto(product)).thenReturn(productResponseDto);

        // When
        PageResponseDto<ProductResponseDto> result = productService.getAllProducts(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(productRepository).findAll(pageable);
    }

    @Test
    void updateProduct_ShouldUpdateSuccessfully() {
        // Given
        Product updatedProduct = Product.builder()
                .id(1L)
                .sku("TEST-SKU-001")
                .name("Updated Product")
                .description("Test Description")
                .price(new BigDecimal("149.99"))
                .category(category)
                .brand(brand)
                .status(ProductStatus.ACTIVE)
                .build();

        ProductResponseDto updatedResponseDto = ProductResponseDto.builder()
                .id(1L)
                .sku("TEST-SKU-001")
                .name("Updated Product")
                .description("Test Description")
                .price(new BigDecimal("149.99"))
                .categoryName("Electronics")
                .brandName("Samsung")
                .status(ProductStatus.ACTIVE)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productMapper.toDto(updatedProduct)).thenReturn(updatedResponseDto);

        // When
        ProductResponseDto result = productService.updateProduct(1L, updateRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Product");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("149.99"));

        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
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