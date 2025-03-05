package org.example.auth.service;

import org.example.auth.dto.AuthRequest;
import org.example.auth.dto.AuthResponse;
import org.example.auth.dto.TokenRefreshRequest;
import org.example.auth.dto.TokenRefreshResponse;
import org.example.auth.entity.User;

public interface AuthService {

    User register(AuthRequest request);

    AuthResponse authenticate(AuthRequest request);

    TokenRefreshResponse refreshToken(TokenRefreshRequest request);

    void logout(String refreshToken);
}
