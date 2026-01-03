package com.abhinav.moviebooking.auth.dto.request;

import jakarta.validation.constraints.NotNull;

public class RefreshTokenRequest {

    @NotNull
    private final String refreshToken;

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
