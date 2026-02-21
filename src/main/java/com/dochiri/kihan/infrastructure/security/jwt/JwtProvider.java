package com.dochiri.kihan.infrastructure.security.jwt;

import com.dochiri.kihan.infrastructure.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_CATEGORY = "category";
    private static final String CATEGORY_ACCESS = "access";
    private static final String CATEGORY_REFRESH = "refresh";

    private final JwtProperties jwtProperties;
    private final Clock clock;

    public String generateAccessToken(Long userId, String role) {
        return generateToken(
                userId,
                role,
                CATEGORY_ACCESS,
                jwtProperties.accessExpiration()
        );
    }

    public String generateRefreshToken(Long userId, String role) {
        return generateToken(
                userId,
                role,
                CATEGORY_REFRESH,
                jwtProperties.refreshExpiration()
        );
    }

    public LocalDateTime refreshTokenExpiresAt() {
        return LocalDateTime.now(clock)
                .plus(jwtProperties.refreshExpiration(), ChronoUnit.MILLIS);
    }

    public Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .clock(() -> new Date(clock.millis()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public String extractRole(Claims claims) {
        return claims.get(CLAIM_ROLE, String.class);
    }

    public boolean isAccessToken(Claims claims) {
        return CATEGORY_ACCESS.equals(
                claims.get(CLAIM_CATEGORY, String.class)
        );
    }

    public boolean isRefreshToken(Claims claims) {
        return CATEGORY_REFRESH.equals(
                claims.get(CLAIM_CATEGORY, String.class)
        );
    }

    private String generateToken(
            Long userId,
            String role,
            String category,
            long expirationMillis
    ) {
        long now = clock.millis();

        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_CATEGORY, category)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMillis))
                .signWith(signingKey())
                .compact();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

}
