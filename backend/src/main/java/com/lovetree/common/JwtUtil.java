package com.lovetree.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private static final long EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L;

    public JwtUtil(@Value("${lovetree.jwt.secret:lovetree-secret-change-me}") String secret) {
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String generateToken(Long userId, Long coupleId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("coupleId", coupleId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRATION_MS))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public Long getCoupleId(Claims claims) {
        Object coupleId = claims.get("coupleId");
        if (coupleId == null) {
            return null;
        }
        if (coupleId instanceof Number) {
            return ((Number) coupleId).longValue();
        }
        return Long.parseLong(coupleId.toString());
    }
}
