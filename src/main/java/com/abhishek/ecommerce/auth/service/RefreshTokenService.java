package com.abhishek.ecommerce.auth.service;

import com.abhishek.ecommerce.auth.entity.RefreshToken;
import com.abhishek.ecommerce.user.entity.User;

public interface RefreshTokenService {
    RefreshToken createOrReplaceRefreshToken(User user);
    RefreshToken validateAndGet(String token);
    void deleteByUsername(String username);
}

