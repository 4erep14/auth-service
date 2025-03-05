package org.example.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.auth.dto.AuthRequest;
import org.example.auth.dto.AuthResponse;
import org.example.auth.dto.TokenRefreshRequest;
import org.example.auth.dto.TokenRefreshResponse;
import org.example.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication Controller", description = "Endpoints for authentication operations")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user with provided credentials")
    public ResponseEntity<?> register(@RequestBody @Valid AuthRequest request) {
        authService.register(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns access and refresh tokens")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
        return ResponseEntity.ok().body(authService.authenticate(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refreshes the JWT tokens using a valid refresh token")
    public ResponseEntity<TokenRefreshResponse> refresh(@RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok().body(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidates the provided refresh token to log the user out")
    public ResponseEntity<?> logout(@RequestBody TokenRefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok().build();
    }
}