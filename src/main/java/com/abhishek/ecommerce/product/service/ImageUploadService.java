package com.abhishek.ecommerce.product.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service for handling product image uploads to Cloudinary
 */
public interface ImageUploadService {

    /**
     * Upload a single image to Cloudinary
     * 
     * @param file the image file to upload
     * @return the URL of the uploaded image
     * @throws IllegalArgumentException if file is invalid
     */
    String uploadImage(MultipartFile file);

    /**
     * Upload multiple images to Cloudinary
     * 
     * @param files list of image files to upload
     * @return list of URLs of uploaded images
     * @throws IllegalArgumentException if any file is invalid
     */
    List<String> uploadImages(List<MultipartFile> files);

    /**
     * Delete an image from Cloudinary
     * 
     * @param imageUrl the URL of the image to delete
     */
    void deleteImage(String imageUrl);
}
