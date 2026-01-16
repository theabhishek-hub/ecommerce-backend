package com.abhishek.ecommerce.seller.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO for seller response
 */
@Getter
@Setter
public class SellerResponseDto {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String approvedByAdminEmail;
    private String rejectionReason;

}
