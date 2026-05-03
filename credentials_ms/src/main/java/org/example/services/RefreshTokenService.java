package org.example.services;

import org.example.entities.Credential;
import org.example.entities.RefreshToken;
import org.example.repositories.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public String createRefreshToken(Credential credential) {
        // Generăm un string aleator
        String token = UUID.randomUUID().toString();

        // Setăm expirarea la 7 zile
        RefreshToken refreshToken = new RefreshToken(
                credential,
                token,
                LocalDateTime.now().plusDays(7)
        );

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    public RefreshToken validateAndGetToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(t -> !t.isRevoked())
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid, expired, or revoked refresh token"));
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }
}