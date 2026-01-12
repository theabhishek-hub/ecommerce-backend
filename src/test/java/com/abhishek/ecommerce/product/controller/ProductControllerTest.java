package com.abhishek.ecommerce.product.controller;

import com.abhishek.ecommerce.common.api.ApiResponse;
import com.abhishek.ecommerce.common.api.PageResponseDto;
import com.abhishek.ecommerce.product.dto.request.ProductCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.ProductUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;
import com.abhishek.ecommerce.product.entity.ProductStatus;
import com.abhishek.ecommerce.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductResponseDto productResponseDto;
    private ProductCreateRequestDto createRequestDto;
    private ProductUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
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
    @WithMockUser(roles = "ADMIN")
    void createProduct_ShouldCreateProductSuccessfully() throws Exception {
        // Given
        when(productService.createProduct(any(ProductCreateRequestDto.class)))
                .thenReturn(productResponseDto);

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product created successfully"))
                .andExpect(jsonPath("$.data.sku").value("TEST-SKU-001"))
                .andExpect(jsonPath("$.data.name").value("Test Product"));
    }

    @Test
    void createProduct_ShouldReturn403_WhenNotAdmin() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        // Given
        when(productService.getProductById(1L)).thenReturn(productResponseDto);

        // When & Then
        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.sku").value("TEST-SKU-001"));
    }

    @Test
    void getAllProducts_ShouldReturnProductList() throws Exception {
        // Given
        List<ProductResponseDto> products = List.of(productResponseDto);
        when(productService.getAllProducts()).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].sku").value("TEST-SKU-001"));
    }

    @Test
    void getAllProducts_WithPagination_ShouldReturnPagedResult() throws Exception {
        // Given
        PageResponseDto<ProductResponseDto> pageResponse = PageResponseDto.<ProductResponseDto>builder()
                .content(List.of(productResponseDto))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(productService.getAllProducts(any(Pageable.class))).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/products/paged")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_ShouldUpdateSuccessfully() throws Exception {
        // Given
        ProductResponseDto updatedResponse = new ProductResponseDto();
        updatedResponse.setId(1L);
        updatedResponse.setSku("TEST-SKU-001");
        updatedResponse.setName("Updated Product");
        updatedResponse.setPriceAmount(new BigDecimal("149.99"));
        updatedResponse.setCurrency("USD");

        when(productService.updateProduct(eq(1L), any(ProductUpdateRequestDto.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/products/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Product"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_ShouldDeleteSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/products/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateProduct_ShouldActivateSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/v1/products/1/activate")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateProduct_ShouldDeactivateSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/v1/products/1/deactivate")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getProductsByCategory_ShouldReturnFilteredProducts() throws Exception {
        // Given
        PageResponseDto<ProductResponseDto> pageResponse = PageResponseDto.<ProductResponseDto>builder()
                .content(List.of(productResponseDto))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(productService.getProductsByCategory(eq(1L), any(Pageable.class)))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/products/category/1")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].sku").value("TEST-SKU-001"));
    }

    @Test
    void searchProductsByName_ShouldReturnSearchResults() throws Exception {
        // Given
        PageResponseDto<ProductResponseDto> pageResponse = PageResponseDto.<ProductResponseDto>builder()
                .content(List.of(productResponseDto))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(productService.searchProductsByName(eq("test"), any(Pageable.class)))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/products/search")
                .param("name", "test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Test Product"));
    }
}