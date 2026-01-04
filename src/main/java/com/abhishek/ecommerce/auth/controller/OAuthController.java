package com.abhishek.ecommerce.auth.controller;

import com.abhishek.ecommerce.auth.dto.OAuthResponseDto;
import com.abhishek.ecommerce.auth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/success")
    public OAuthResponseDto oauthSuccess(
            @AuthenticationPrincipal OAuth2User oAuth2User
    ) {
        return oAuthService.handleOAuthLogin(oAuth2User, "GOOGLE");
    }
}

