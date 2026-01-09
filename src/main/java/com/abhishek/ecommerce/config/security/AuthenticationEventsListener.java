package com.abhishek.ecommerce.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthenticationEventsListener {

    @Async("taskExecutor")
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        log.info("Authentication success for {}", username);
    }

    @Async("taskExecutor")
    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = (event.getAuthentication() != null) ? event.getAuthentication().getName() : "unknown";
        log.warn("Authentication failure for {}", username);
    }
}

