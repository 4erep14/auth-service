package org.example.auth.dto;

public record TokenRefreshResponse(
        String accessToken,
        String refreshToken
) {
}
