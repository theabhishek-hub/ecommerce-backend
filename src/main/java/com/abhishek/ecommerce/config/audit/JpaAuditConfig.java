package com.abhishek.ecommerce.config.audit;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// @Configuration
// @EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
public class JpaAuditConfig {
    // SpringSecurityAuditorAware is now a @Component, so Spring will auto-detect it
}


