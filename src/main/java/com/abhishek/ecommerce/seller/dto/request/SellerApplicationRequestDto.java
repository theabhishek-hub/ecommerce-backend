package com.abhishek.ecommerce.seller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for seller application request with full business details
 */
@Getter
@Setter
public class SellerApplicationRequestDto {

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Business description is required")
    private String businessDescription;

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN format")
    private String panNumber;

    private String gstNumber;

    @NotBlank(message = "Street address is required")
    private String streetAddress;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Postal code is required")
    private String postalCode;

    private String country = "India";

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Bank account number is required")
    private String bankAccountNumber;

    @NotBlank(message = "Bank IFSC code is required")
    private String bankIfscCode;

}
