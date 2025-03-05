package org.example.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.private-key}")
    private String privateKeyStr;

    @Value("${jwt.public-key}")
    private String publicKeyStr;

    @Value("${jwt.access.expiration}")
    private int accessExpirationM;

    @Value("${jwt.refresh.expiration}")
    private int refreshExpirationM;

    private final static int MINUTEST = 1000 * 60;

    private PrivateKey getPrivateKey() throws Exception {
        String normalizedKey = privateKeyStr
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(normalizedKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public PublicKey getPublicKey() throws Exception {
        String normalizedKey = publicKeyStr
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(normalizedKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    public String generateAccessToken(String username, long userId) {
        return generateToken(username, userId, accessExpirationM);
    }

    public String generateRefreshToken(String username, long userId) {
        return generateToken(username, userId, refreshExpirationM);
    }

    private String generateToken(String subject, long userId, long expirationM) {
        try {
            PrivateKey privateKey = getPrivateKey();
            return Jwts.builder()
                    .subject(subject)
                    .claim("userId", userId)
                    .claim("userEmail", subject)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + expirationM * MINUTEST))
                    .signWith(privateKey)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    // Extract the subject (username) from the token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract the custom userId claim from the token
    public long extractUserId(String token) {
        return extractClaim(token, claims -> ((Number) claims.get("userId")).longValue());
    }

    // Generic method to extract any claim from the token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            PublicKey publicKey = getPublicKey();
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract claim from token", e);
        }
    }

    // Validate that the token is correct, belongs to the specified user, and is not expired
    public boolean isTokenValid(String token, String username) {
        try {
            String extractedUsername = extractUsername(token);
            return extractedUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Check if the token has expired
    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }
}