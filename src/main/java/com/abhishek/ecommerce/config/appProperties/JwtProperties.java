package com.abhishek.ecommerce.config.appProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Validated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtProperties {

    @NotBlank
    private String accessSecret;

    @NotBlank
    private String refreshSecret;

    @Positive
    private long accessTokenExpiration; // in ms

    @Positive
    private long refreshTokenExpiration; // in ms

}
