package com.abhishek.ecommerce.seller.exception;

import com.abhishek.ecommerce.common.exception.BusinessException;

public class SellerNotFoundException extends BusinessException {

    public SellerNotFoundException(Long id) {
        super("Seller not found with id: " + id, "SELLER_NOT_FOUND");
    }

    public SellerNotFoundException(String message) {
        super(message, "SELLER_NOT_FOUND");
    }
}
