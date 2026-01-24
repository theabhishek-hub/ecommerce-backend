package com.abhishek.ecommerce.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating/updating a seller application
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerApplicationRequest {

    @NotBlank(message = "Business name is required")
    private String businessName;

    private String businessType; // e.g., INDIVIDUAL, PARTNERSHIP, COMPANY

    private String businessDescription;

    // Tax Information
    private String pan; // PAN number

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
    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Account holder name is required")
    private String accountHolderName;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    private String ifscCode;

    private String swiftCode;

    // Document URLs (would be uploaded via separate file upload endpoint)
    private String panDocumentUrl;

    private String gstDocumentUrl;

    private String businessProofUrl;

    private String bankProofUrl;

    private String additionalNotes;

}
