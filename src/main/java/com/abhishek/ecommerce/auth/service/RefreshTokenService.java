package com.abhishek.ecommerce.auth.service;

import com.abhishek.ecommerce.auth.entity.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createOrReplaceRefreshToken(String username, long expiresInMs, String token);
    RefreshToken validateAndGet(String token);
    void deleteByUsername(String username);
}

