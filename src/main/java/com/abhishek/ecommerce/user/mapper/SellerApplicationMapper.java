package com.abhishek.ecommerce.user.mapper;

import com.abhishek.ecommerce.user.dto.request.SellerApplicationRequest;
import com.abhishek.ecommerce.user.dto.response.SellerApplicationResponse;
import com.abhishek.ecommerce.user.entity.SellerApplication;
import org.springframework.stereotype.Component;

/**
 * Mapper for SellerApplication entity <-> DTOs
 */
@Component
public class SellerApplicationMapper {

    /**
     * Map SellerApplication entity to SellerApplicationResponse DTO
     */
    public SellerApplicationResponse toResponse(SellerApplication entity) {
        if (entity == null) {
            return null;
        }

        SellerApplicationResponse response = new SellerApplicationResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUser() != null ? entity.getUser().getId() : null);

        // Business Details
        response.setBusinessName(entity.getBusinessName());
        response.setBusinessType(entity.getBusinessType());
        response.setBusinessDescription(entity.getBusinessDescription());

        // Tax Information
        response.setPan(entity.getPan());
        response.setGstNumber(entity.getGstNumber());
        response.setTaxId(entity.getTaxId());

        // Business Address
        response.setAddressLine1(entity.getAddressLine1());
        response.setAddressLine2(entity.getAddressLine2());
        response.setCity(entity.getCity());
        response.setState(entity.getState());
        response.setPostalCode(entity.getPostalCode());
        response.setCountry(entity.getCountry());

        // Bank Information
        response.setBankName(entity.getBankName());
        response.setAccountHolderName(entity.getAccountHolderName());
        response.setAccountNumber(maskAccountNumber(entity.getAccountNumber()));
        response.setIfscCode(entity.getIfscCode());
        response.setSwiftCode(entity.getSwiftCode());

        // Document URLs
        response.setPanDocumentUrl(entity.getPanDocumentUrl());
        response.setGstDocumentUrl(entity.getGstDocumentUrl());
        response.setBusinessProofUrl(entity.getBusinessProofUrl());
        response.setBankProofUrl(entity.getBankProofUrl());

        // Status
        response.setStatus(entity.getStatus());
        response.setSubmissionDate(entity.getSubmissionDate());
        response.setReviewDate(entity.getReviewDate());
        response.setRejectionReason(entity.getRejectionReason());
        response.setAdditionalNotes(entity.getAdditionalNotes());

        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        return response;
    }

    /**
     * Map SellerApplicationRequest DTO to SellerApplication entity
     */
    public SellerApplication toEntity(SellerApplicationRequest request) {
        if (request == null) {
            return null;
        }

        SellerApplication entity = new SellerApplication();

        // Business Details
        entity.setBusinessName(request.getBusinessName());
        entity.setBusinessType(request.getBusinessType());
        entity.setBusinessDescription(request.getBusinessDescription());

        // Tax Information
        entity.setPan(request.getPan());
        entity.setGstNumber(request.getGstNumber());
        entity.setTaxId(request.getTaxId());

        // Business Address
        entity.setAddressLine1(request.getAddressLine1());
        entity.setAddressLine2(request.getAddressLine2());
        entity.setCity(request.getCity());
        entity.setState(request.getState());
        entity.setPostalCode(request.getPostalCode());
        entity.setCountry(request.getCountry());

        // Bank Information
        entity.setBankName(request.getBankName());
        entity.setAccountHolderName(request.getAccountHolderName());
        entity.setAccountNumber(request.getAccountNumber());
        entity.setIfscCode(request.getIfscCode());
        entity.setSwiftCode(request.getSwiftCode());

        // Document URLs
        entity.setPanDocumentUrl(request.getPanDocumentUrl());
        entity.setGstDocumentUrl(request.getGstDocumentUrl());
        entity.setBusinessProofUrl(request.getBusinessProofUrl());
        entity.setBankProofUrl(request.getBankProofUrl());

        entity.setAdditionalNotes(request.getAdditionalNotes());

        return entity;
    }

    /**
     * Update SellerApplication entity from SellerApplicationRequest DTO
     */
    public void updateEntity(SellerApplicationRequest request, SellerApplication entity) {
        if (request == null || entity == null) {
            return;
        }

        // Business Details
        entity.setBusinessName(request.getBusinessName());
        entity.setBusinessType(request.getBusinessType());
        entity.setBusinessDescription(request.getBusinessDescription());

        // Tax Information
        entity.setPan(request.getPan());
        entity.setGstNumber(request.getGstNumber());
        entity.setTaxId(request.getTaxId());

        // Business Address
        entity.setAddressLine1(request.getAddressLine1());
        entity.setAddressLine2(request.getAddressLine2());
        entity.setCity(request.getCity());
        entity.setState(request.getState());
        entity.setPostalCode(request.getPostalCode());
        entity.setCountry(request.getCountry());

        // Bank Information
        entity.setBankName(request.getBankName());
        entity.setAccountHolderName(request.getAccountHolderName());
        entity.setAccountNumber(request.getAccountNumber());
        entity.setIfscCode(request.getIfscCode());
        entity.setSwiftCode(request.getSwiftCode());

        // Document URLs
        entity.setPanDocumentUrl(request.getPanDocumentUrl());
        entity.setGstDocumentUrl(request.getGstDocumentUrl());
        entity.setBusinessProofUrl(request.getBusinessProofUrl());
        entity.setBankProofUrl(request.getBankProofUrl());

        entity.setAdditionalNotes(request.getAdditionalNotes());
    }

    /**
     * Mask account number for security (show only last 4 digits)
     */
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return "****";
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

}
