package com.abhishek.ecommerce.seller.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO for seller response with all business details
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
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private String approvedByAdminEmail;
    private String rejectionReason;

    // Business details
    private String businessName;
    private String businessDescription;
    private String panNumber;
    private String gstNumber;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phoneNumber;
    private String bankAccountNumber;
    private String bankIfscCode;

}
