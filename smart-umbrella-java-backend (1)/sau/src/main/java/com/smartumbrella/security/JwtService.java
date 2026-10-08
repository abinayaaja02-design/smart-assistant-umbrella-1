package com.smartumbrella.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Issues and verifies login tokens (JWT) for the web dashboard. */
@Service
public class JwtService {
    private final SecretKey key;
    private final long expiryMs;

    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiry-minutes}") long minutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMs = minutes * 60_000;
    }

    public String issue(Long userId) {
        return Jwts.builder().subject(userId.toString())
            .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + expiryMs))
            .signWith(key).compact();
    }

    public Long verify(String token) {
        return Long.valueOf(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject());
    }
}
