package org.example.auth.dto;

public record TokenRefreshRequest(
        String refreshToken
) {
}
