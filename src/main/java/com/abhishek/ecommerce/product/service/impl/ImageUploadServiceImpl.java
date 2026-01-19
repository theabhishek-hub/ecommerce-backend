package com.abhishek.ecommerce.product.service.impl;

import com.abhishek.ecommerce.config.cloudinaryConfig.CloudinaryProperties;
import com.abhishek.ecommerce.product.exception.ImageUploadException;
import com.abhishek.ecommerce.product.service.ImageUploadService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Image upload service implementation using Cloudinary
 * 
 * Validation rules:
 * - Allowed formats: JPG, PNG
 * - Max file size: 1 MB per image
 * - Automatically rejects non-image files
 */
@Slf4j
@Service
public class ImageUploadServiceImpl implements ImageUploadService {

    private final Optional<Cloudinary> cloudinary;
    private final CloudinaryProperties cloudinaryProperties;

    @Autowired
    public ImageUploadServiceImpl(Optional<Cloudinary> cloudinary, CloudinaryProperties cloudinaryProperties) {
        this.cloudinary = cloudinary;
        this.cloudinaryProperties = cloudinaryProperties;
    }

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png");
    private static final long MAX_FILE_SIZE = 1024 * 1024; // 1 MB
    private static final String UPLOAD_FOLDER = "ecommerce/products";

    @Override
    public String uploadImage(MultipartFile file) {
        validateFile(file);
        
        if (!cloudinaryProperties.isEnabled() || cloudinary.isEmpty()) {
            throw new ImageUploadException("CLOUDINARY_NOT_CONFIGURED", "Cloudinary is not configured");
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.get().uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", UPLOAD_FOLDER,
                            "resource_type", "auto",
                            "overwrite", false
                    )
            );

            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("Image uploaded successfully to Cloudinary: {}", imageUrl);
            return imageUrl;

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new ImageUploadException("CLOUDINARY_UPLOAD_FAILED", "Failed to upload image: " + e.getMessage());
        }
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided for upload");
        }

        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String url = uploadImage(file);
                uploadedUrls.add(url);
            }
        }

        return uploadedUrls;
    }

    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        if (!cloudinaryProperties.isEnabled() || cloudinary.isEmpty()) {
            log.warn("Cloudinary not configured, cannot delete image");
            return;
        }

        try {
            // Extract public ID from URL
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId != null) {
                cloudinary.get().uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Image deleted from Cloudinary: {}", publicId);
            }
        } catch (Exception e) {
            log.error("Failed to delete image from Cloudinary: {}", imageUrl, e);
            // Don't throw exception - log warning and continue
        }
    }

    /**
     * Validate file before upload
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        // Check content type
        String contentType = file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file format. Only JPG and PNG are allowed. Received: " + contentType
            );
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            long sizeInMB = file.getSize() / (1024 * 1024);
            throw new IllegalArgumentException(
                    "File size exceeds 1 MB limit. Received: " + sizeInMB + " MB"
            );
        }

        // Verify file extension matches content type
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String extension = getFileExtension(filename).toLowerCase();
            if (!isValidExtensionForContentType(extension, contentType)) {
                throw new IllegalArgumentException(
                        "File extension does not match content type"
                );
            }
        }
    }

    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }

    /**
     * Check if file extension matches the content type
     */
    private boolean isValidExtensionForContentType(String extension, String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> extension.equals("jpg") || extension.equals("jpeg");
            case "image/png" -> extension.equals("png");
            default -> false;
        };
    }

    /**
     * Extract public ID from Cloudinary URL
     * URL format: https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{public_id}.{format}
     */
    private String extractPublicIdFromUrl(String url) {
        try {
            if (!url.contains("cloudinary.com")) {
                return null;
            }

            // Extract the part after /upload/
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) {
                return null;
            }

            String afterUpload = url.substring(uploadIndex + 8);
            
            // Skip version info like v1234567890/
            if (afterUpload.matches("^v\\d+/.*")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
            }

            // Remove file extension (.jpg, .png, etc.)
            int lastDot = afterUpload.lastIndexOf('.');
            if (lastDot > 0) {
                afterUpload = afterUpload.substring(0, lastDot);
            }

            return afterUpload;
        } catch (Exception e) {
            log.warn("Failed to extract public ID from URL: {}", url, e);
            return null;
        }
    }
}
