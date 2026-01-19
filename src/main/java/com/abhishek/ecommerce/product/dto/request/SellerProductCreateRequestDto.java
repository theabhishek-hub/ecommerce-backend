package com.abhishek.ecommerce.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seller product creation DTO with image upload support
 * Extends ProductCreateRequestDto to add MultipartFile image handling
 * 
 * Validation rules:
 * - Minimum 1 image required
 * - Maximum 2 images allowed
 * - Only JPG/PNG accepted
 * - Max 1 MB per image
 */
@Getter
@Setter
public class SellerProductCreateRequestDto {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Price amount is required")
    @Positive(message = "Price amount must be positive")
    private BigDecimal priceAmount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Brand ID is required")
    private Long brandId;

    /**
     * Image files for upload (1-2 images)
     * Validation handled in service layer
     */
    private List<MultipartFile> imageFiles;

    /**
     * Convert to ProductCreateRequestDto for service use
     * This is used after images are uploaded and only the URL is needed
     */
    public ProductCreateRequestDto toProductCreateRequestDto(String imageUrl) {
        ProductCreateRequestDto dto = new ProductCreateRequestDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setPriceAmount(priceAmount);
        dto.setCurrency(currency);
        dto.setSku(sku);
        dto.setCategoryId(categoryId);
        dto.setBrandId(brandId);
        dto.setImageUrl(imageUrl);
        return dto;
    }
}
