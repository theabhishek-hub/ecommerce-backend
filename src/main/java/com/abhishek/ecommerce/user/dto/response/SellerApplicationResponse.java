package com.abhishek.ecommerce.user.dto.response;

import com.abhishek.ecommerce.shared.enums.SellerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for seller application response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerApplicationResponse {

    private Long id;

    private Long userId;

    // Business Details
    private String businessName;

    private String businessType;

    private String businessDescription;

    // Tax Information
    private String pan;

    private String gstNumber;

    private String taxId;

    // Business Address
    private String addressLine1;

    private String addressLine2;

    private String city;

    private String state;

    private String postalCode;

    private String country;

    // Bank Information
    private String bankName;

    private String accountHolderName;

    private String accountNumber;

    private String ifscCode;

    private String swiftCode;

    // Document URLs
    private String panDocumentUrl;

    private String gstDocumentUrl;

    private String businessProofUrl;

    private String bankProofUrl;

    // Status
    private SellerStatus status;

    private LocalDateTime submissionDate;

    private LocalDateTime reviewDate;

    private String rejectionReason;

    private String additionalNotes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
