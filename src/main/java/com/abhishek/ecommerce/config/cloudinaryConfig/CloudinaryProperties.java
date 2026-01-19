package com.abhishek.ecommerce.config.cloudinaryConfig;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cloudinary configuration properties
 * Binds from environment variables via application.yml:
 * cloudinary.cloud-name -> ${CLOUDINARY_CLOUD_NAME:}
 * cloudinary.api-key -> ${CLOUDINARY_API_KEY:}
 * cloudinary.api-secret -> ${CLOUDINARY_API_SECRET:}
 */
@Component
@ConfigurationProperties(prefix = "cloudinary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CloudinaryProperties {

    private String cloudName = "";
    private String apiKey = "";
    private String apiSecret = "";

    /**
     * Check if Cloudinary is configured
     */
    public boolean isEnabled() {
        return !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
    }
}
