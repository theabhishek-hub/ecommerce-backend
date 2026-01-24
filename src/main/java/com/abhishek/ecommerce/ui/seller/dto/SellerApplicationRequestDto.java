package com.abhishek.ecommerce.ui.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for seller application form submission.
 * Contains all fields required for a user to apply as a seller.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerApplicationRequestDto {

    // Business Information
    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Business description is required")
    private String businessDescription;

    // Tax Information
    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN format. Example: AAAAA0000A")
    private String panNumber;

    private String gstNumber;

    // Address Information
    @NotBlank(message = "Street address is required")
    private String streetAddress;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Postal code is required")
    private String postalCode;

    private String country;

    // Contact Information
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    // Bank Information
    @NotBlank(message = "Bank account number is required")
    private String bankAccountNumber;

    @NotBlank(message = "Bank IFSC code is required")
    private String bankIfscCode;
}
