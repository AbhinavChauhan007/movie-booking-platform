package com.abhinav.moviebooking.security.token.service;


import com.abhinav.moviebooking.security.token.entity.RefreshToken;
import com.abhinav.moviebooking.security.token.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final long REFRESH_TOKEN_EXPIRY =
            7 * 24 * 60 * 60; // 7 days

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(String username) {
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken(
                token,
                username,
                Instant.now().plusSeconds(REFRESH_TOKEN_EXPIRY),
                Set.of("ROLE_USER"), // will change it later
                false
        );
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.isRevoked())
            throw new RuntimeException("Refresh token revoked");

        if (refreshToken.getExpiry().isBefore(Instant.now()))
            throw new RuntimeException("Refresh token expired");

        return refreshToken;
    }


    // ================= REVOKE SINGLE =================
    public void revokeToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    // ================= REVOKE ALL FOR USER ===========
    public void revokeAllTokensForUser(String username) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUsernameAndRevokedFalse(username);

        tokens.forEach(token -> token.setRevoked(true));

        refreshTokenRepository.saveAll(tokens);
    }
}
