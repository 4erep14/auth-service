package org.example.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.auth.dto.AuthRequest;
import org.example.auth.dto.AuthResponse;
import org.example.auth.dto.TokenRefreshRequest;
import org.example.auth.dto.TokenRefreshResponse;
import org.example.auth.entity.RefreshToken;
import org.example.auth.entity.User;
import org.example.auth.exception.InvalidRefreshTokenException;
import org.example.auth.exception.UserAlreadyExistsException;
import org.example.auth.repository.RefreshTokenRepository;
import org.example.auth.repository.UserRepository;
import org.example.auth.security.JwtUtil;
import org.example.auth.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public User register(AuthRequest request) {
        userRepository.findByUsername(request.getUsername())
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException(
                            "User with username " + request.getUsername() + " already exists"
                    );
                });

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();

        return userRepository.save(user);
    }

    public AuthResponse authenticate(AuthRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        authenticationManager.authenticate(authenticationToken);

        long userId = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + " not found")).getId();

        String accessToken = jwtUtil.generateAccessToken(request.getUsername(), userId);
        String refreshToken = jwtUtil.generateRefreshToken(request.getUsername(), userId);

        RefreshToken rt = RefreshToken.builder()
                .token(refreshToken)
                .username(request.getUsername())
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .invalidated(false)
                .build();
        System.out.println(rt.getToken());
        refreshTokenRepository.save(rt);

        return new AuthResponse(accessToken, refreshToken);
    }

    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException(
                        "Refresh token not found."
                ));

        if (storedToken.isInvalidated() || storedToken.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Refresh token is invalid or expired.");
        }

        long userId = jwtUtil.extractUserId(request.refreshToken());


        String newAccessToken = jwtUtil.generateAccessToken(storedToken.getUsername(), userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(storedToken.getUsername(), userId);

        storedToken.setToken(newRefreshToken);
        storedToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        storedToken.setInvalidated(false);
        refreshTokenRepository.save(storedToken);

        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }

    public  void logout(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException(
                        "Refresh token not found."
                ));
        storedToken.setInvalidated(true);
        refreshTokenRepository.save(storedToken);
    }


}
