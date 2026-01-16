package com.abhishek.ecommerce.seller.mapper;

import com.abhishek.ecommerce.seller.dto.response.SellerResponseDto;
import com.abhishek.ecommerce.seller.entity.Seller;
import org.springframework.stereotype.Component;

@Component
public class SellerMapper {

    /**
     * Map Seller entity to response DTO
     */
    public SellerResponseDto toDto(Seller seller) {
        if (seller == null) {
            return null;
        }

        SellerResponseDto dto = new SellerResponseDto();
        dto.setId(seller.getId());
        dto.setUserId(seller.getUser() != null ? seller.getUser().getId() : null);
        dto.setUserEmail(seller.getUser() != null ? seller.getUser().getEmail() : null);
        dto.setUserFullName(seller.getUser() != null ? seller.getUser().getFullName() : null);
        dto.setStatus(seller.getStatus() != null ? seller.getStatus().name() : null);
        dto.setCreatedAt(seller.getCreatedAt());
        dto.setApprovedAt(seller.getApprovedAt());
        dto.setApprovedByAdminEmail(seller.getApprovedByAdmin() != null ? seller.getApprovedByAdmin().getEmail() : null);
        dto.setRejectionReason(seller.getRejectionReason());

        return dto;
    }

}
