package com.abhishek.ecommerce.config.cloudinaryConfig;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cloudinary configuration bean
 * Creates a Cloudinary instance if credentials are available
 */
@Configuration
@RequiredArgsConstructor
public class CloudinaryConfig {

    private final CloudinaryProperties cloudinaryProperties;

    @Bean
    public Cloudinary cloudinary() {
        if (!cloudinaryProperties.isEnabled()) {
            return null;
        }

        return new Cloudinary(
                "cloudinary://" +
                        cloudinaryProperties.getApiKey() + ":" +
                        cloudinaryProperties.getApiSecret() + "@" +
                        cloudinaryProperties.getCloudName()
        );
    }
}
