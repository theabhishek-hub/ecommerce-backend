package com.abhishek.ecommerce.product.mapper;

import com.abhishek.ecommerce.common.entity.Money;
import com.abhishek.ecommerce.product.dto.request.ProductCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.ProductUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;
import com.abhishek.ecommerce.product.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductMapper {

    // ================= CREATE =================
    public Product toEntity(ProductCreateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(mapToMoney(dto.getPriceAmount(), dto.getCurrency()));

        return product;
    }

    // ================= RESPONSE =================
    public ProductResponseDto toDto(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPriceAmount(product.getPrice() != null ? product.getPrice().getAmount() : null);
        dto.setCurrency(product.getPrice() != null ? product.getPrice().getCurrency() : null);
        dto.setStatus(product.getStatus() != null ? product.getStatus().name() : null);
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        dto.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
        dto.setBrandId(product.getBrand() != null ? product.getBrand().getId() : null);
        dto.setBrandName(product.getBrand() != null ? product.getBrand().getName() : null);

        return dto;
    }

    // ================= UPDATE =================
    public void updateEntityFromDto(ProductUpdateRequestDto dto, Product product) {
        if (dto == null || product == null) {
            return;
        }

        if (dto.getName() != null) {
            product.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getPriceAmount() != null && dto.getCurrency() != null) {
            product.setPrice(mapToMoney(dto.getPriceAmount(), dto.getCurrency()));
        }
    }

    public Money mapToMoney(BigDecimal amount, String currency) {
        if (amount == null || currency == null) {
            return null;
        }
        return new Money(amount, currency);
    }

    // Helper method for service layer
    public Money mapToMoneyFromDto(BigDecimal amount, String currency) {
        return mapToMoney(amount, currency);
    }
}

