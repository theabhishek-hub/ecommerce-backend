package com.abhishek.ecommerce.product.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductResponseDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal priceAmount;
    private String currency;
    private String sku;
    private String imageUrl;
    private String status;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    
    // Seller information
    private Long sellerId;
    private String sellerEmail;
    private String sellerFullName;
    
    // Inventory information
    private Integer quantity;
    
    // Creator information
    private String createdBy;

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPriceAmount(BigDecimal priceAmount) {
        this.priceAmount = priceAmount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public void setSellerEmail(String sellerEmail) {
        this.sellerEmail = sellerEmail;
    }

    public void setSellerFullName(String sellerFullName) {
        this.sellerFullName = sellerFullName;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}