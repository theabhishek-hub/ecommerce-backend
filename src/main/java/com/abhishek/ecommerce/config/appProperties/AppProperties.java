package com.abhishek.ecommerce.config.appProperties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppProperties {

    // Application-level name and other global metadata
    private String name;

}