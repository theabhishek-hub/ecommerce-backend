package com.abhishek.ecommerce.auth.controller;

import com.abhishek.ecommerce.auth.dto.OAuthResponseDto;
import com.abhishek.ecommerce.auth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@Tag(name = "OAuth2", description = "OAuth2 authentication (Google)")
@RestController
@RequestMapping("/api/v1/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @Operation(
        summary = "OAuth2 login success",
        description = "Handles successful OAuth2 authentication and returns user tokens"
    )
    @GetMapping("/success")
    public OAuthResponseDto oauthSuccess(
            @AuthenticationPrincipal OAuth2User oAuth2User
    ) {
        return oAuthService.handleOAuthLogin(oAuth2User, "GOOGLE");
    }
}

