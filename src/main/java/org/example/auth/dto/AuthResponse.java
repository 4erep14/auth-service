package org.example.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
