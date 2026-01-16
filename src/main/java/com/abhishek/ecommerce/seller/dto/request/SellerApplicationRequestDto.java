package com.abhishek.ecommerce.seller.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for seller application request
 */
@Getter
@Setter
public class SellerApplicationRequestDto {

    private String businessName;
    private String businessDescription;

}
