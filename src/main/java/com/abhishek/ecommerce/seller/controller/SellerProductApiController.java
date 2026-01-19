package com.abhishek.ecommerce.seller.controller;

import com.abhishek.ecommerce.common.apiResponse.ApiResponse;
import com.abhishek.ecommerce.common.apiResponse.ApiResponseBuilder;
import com.abhishek.ecommerce.product.dto.request.SellerProductCreateRequestDto;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;
import com.abhishek.ecommerce.product.service.ImageUploadService;
import com.abhishek.ecommerce.product.service.ProductService;
import com.abhishek.ecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Seller Product API Controller
 * Handles seller-specific product operations including image upload
 * Access: ROLE_SELLER only
 */
@Slf4j
@Tag(name = "Seller Products", description = "Seller product management APIs with image upload")
@RestController
@RequestMapping("/api/v1/seller/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerProductApiController {

    private final ProductService productService;
    private final ImageUploadService imageUploadService;
    private final UserService userService;

    /**
     * POST /api/v1/seller/products/upload-with-images
     * Create seller product with image upload
     * 
     * Multipart request with:
     * - productData: JSON form data
     * - images: 1-2 image files (JPG/PNG, max 1MB each)
     */
    @Operation(
            summary = "Create seller product with image upload",
            description = "Upload 1-2 images and create product. Requires SELLER role"
    )
    @PostMapping(value = "/upload-with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Object createProductWithImages(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("priceAmount") String priceAmount,
            @RequestParam("currency") String currency,
            @RequestParam("sku") String sku,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("brandId") Long brandId,
            @RequestParam(value = "images", required = false) List<MultipartFile> images
    ) {
        try {
            var currentUser = userService.getCurrentUserProfile();
            log.info("Creating seller product with images for userId={}", currentUser.getId());

            // Validate image count
            if (images == null || images.isEmpty()) {
                return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, "At least 1 image is required");
            }
            if (images.size() > 2) {
                return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, "Maximum 2 images allowed");
            }

            // Remove empty files
            images = images.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .toList();

            if (images.isEmpty()) {
                return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, "At least 1 valid image is required");
            }

            if (images.size() > 2) {
                return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, "Maximum 2 images allowed");
            }

            // Upload images
            List<String> imageUrls = imageUploadService.uploadImages(images);
            String primaryImageUrl = imageUrls.get(0); // Use first image as primary

            log.info("Images uploaded successfully: {} images", imageUrls.size());

            // Create product
            SellerProductCreateRequestDto sellerDto = new SellerProductCreateRequestDto();
            sellerDto.setName(name);
            sellerDto.setDescription(description);
            sellerDto.setPriceAmount(new java.math.BigDecimal(priceAmount));
            sellerDto.setCurrency(currency);
            sellerDto.setSku(sku);
            sellerDto.setCategoryId(categoryId);
            sellerDto.setBrandId(brandId);

            // Convert to standard DTO and create product
            com.abhishek.ecommerce.product.dto.request.ProductCreateRequestDto productDto
                    = sellerDto.toProductCreateRequestDto(primaryImageUrl);

            ProductResponseDto response = productService.createProduct(productDto);

            log.info("Seller product created successfully. ProductId={}, Images={}", 
                    response.getId(), imageUrls.size());

            return ApiResponseBuilder.created("Product created with " + imageUrls.size() + " image(s)", response);

        } catch (IllegalArgumentException e) {
            log.warn("Image validation failed: {}", e.getMessage());
            return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error creating seller product with images", e);
            return ApiResponseBuilder.failed(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create product: " + e.getMessage());
        }
    }

    /**
     * POST /api/v1/seller/products/validate-images
     * Validate image files before upload (client-side validation helper)
     */
    @Operation(
            summary = "Validate seller product images",
            description = "Validate image files without uploading them"
    )
    @PostMapping(value = "/validate-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Object validateImages(
            @RequestParam("images") List<MultipartFile> images
    ) {
        try {
            if (images == null || images.isEmpty()) {
                return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, "At least 1 image is required");
            }

            images = images.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .toList();

            if (images.isEmpty()) {
                return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, "At least 1 valid image is required");
            }

            if (images.size() > 2) {
                return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, "Maximum 2 images allowed. You provided " + images.size());
            }

            // Validate each image
            for (MultipartFile image : images) {
                validateImageFile(image);
            }

            log.info("Image validation passed for {} images", images.size());
            return ApiResponseBuilder.success("All images are valid", "OK");

        } catch (IllegalArgumentException e) {
            log.warn("Image validation failed: {}", e.getMessage());
            return ApiResponseBuilder.failed(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Validate a single image file
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file format: " + file.getOriginalFilename() + 
                    ". Only JPG and PNG are allowed"
            );
        }

        long maxSize = 1024 * 1024; // 1 MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    "File size exceeds 1 MB: " + file.getOriginalFilename() + 
                    " (" + (file.getSize() / 1024) + " KB)"
            );
        }
    }
}
