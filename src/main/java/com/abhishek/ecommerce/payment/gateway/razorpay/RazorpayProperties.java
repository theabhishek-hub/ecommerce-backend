package com.abhishek.ecommerce.payment.gateway.razorpay;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "razorpay")
@Getter
@Setter
public class RazorpayProperties {

    /**
     * Bound from application.yml:
     * razorpay.key-id -> env placeholder ${RAZORPAY_KEY_ID:}
     * razorpay.key-secret -> env placeholder ${RAZORPAY_KEY_SECRET:}
     */
    private String keyId = "";
    private String keySecret = "";

    public boolean isEnabled() {
        return !keyId.isBlank() && !keySecret.isBlank();
    }
}

