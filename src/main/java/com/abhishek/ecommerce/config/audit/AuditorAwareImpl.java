package com.abhishek.ecommerce.config.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Will be replaced after Security is added. Default to SYSTEM.
        return Optional.of("SYSTEM");
    }
}
