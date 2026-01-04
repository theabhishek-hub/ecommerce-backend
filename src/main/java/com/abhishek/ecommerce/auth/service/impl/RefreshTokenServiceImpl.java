package com.abhishek.ecommerce.auth.service.impl;

import com.abhishek.ecommerce.auth.entity.RefreshToken;
import com.abhishek.ecommerce.auth.repository.RefreshTokenRepository;
import com.abhishek.ecommerce.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repository;

    @Override
    public RefreshToken createOrReplaceRefreshToken(String username, long expiresInMs, String token) {
        // delete existing tokens for username
        repository.deleteByUsername(username);

        RefreshToken rt = new RefreshToken();
        rt.setToken(token);
        rt.setUsername(username);
        rt.setExpiresAt(Instant.now().plusMillis(expiresInMs));
        return repository.save(rt);
    }

    @Override
    public RefreshToken validateAndGet(String token) {
        Optional<RefreshToken> opt = repository.findByToken(token);
        if (opt.isEmpty()) {
            throw new IllegalStateException("Invalid refresh token");
        }
        RefreshToken rt = opt.get();
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            repository.delete(rt);
            throw new IllegalStateException("Refresh token expired");
        }
        return rt;
    }

    @Override
    public void deleteByUsername(String username) {
        repository.deleteByUsername(username);
    }
}

