package com.abhishek.ecommerce.product.mapper;

import com.abhishek.ecommerce.common.entity.Money;
import com.abhishek.ecommerce.product.dto.request.ProductCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.ProductUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.ProductResponseDto;
import com.abhishek.ecommerce.product.entity.Product;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // ================= CREATE =================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "price", expression = "java(mapToMoney(dto.getPriceAmount(), dto.getCurrency()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Product toEntity(ProductCreateRequestDto dto);

    // ================= RESPONSE =================
    @Mapping(target = "priceAmount", expression = "java(product.getPrice() != null ? product.getPrice().getAmount() : null)")
    @Mapping(target = "currency", expression = "java(product.getPrice() != null ? product.getPrice().getCurrency() : null)")
    @Mapping(target = "status", expression = "java(product.getStatus() != null ? product.getStatus().name() : null)")
    @Mapping(target = "categoryId", expression = "java(product.getCategory() != null ? product.getCategory().getId() : null)")
    @Mapping(target = "categoryName", expression = "java(product.getCategory() != null ? product.getCategory().getName() : null)")
    @Mapping(target = "brandId", expression = "java(product.getBrand() != null ? product.getBrand().getId() : null)")
    @Mapping(target = "brandName", expression = "java(product.getBrand() != null ? product.getBrand().getName() : null)")
    ProductResponseDto toDto(Product product);

    // ================= UPDATE =================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "price", expression = "java(mapToMoney(dto.getPriceAmount(), dto.getCurrency()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromDto(ProductUpdateRequestDto dto, @MappingTarget Product product);

    default Money mapToMoney(BigDecimal amount, String currency) {
        if (amount == null || currency == null) {
            return null;
        }
        return new Money(amount, currency);
    }
    
    // Helper method for service layer
    default Money mapToMoneyFromDto(BigDecimal amount, String currency) {
        return mapToMoney(amount, currency);
    }
}

