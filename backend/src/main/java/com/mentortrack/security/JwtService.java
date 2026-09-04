package com.mentortrack.security;

import com.mentortrack.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // Demo-friendly: pad/derive a stable-length key from the configured secret so any
        // reasonably long secret string works even if it's shorter than 256 bits.
        byte[] rawKey = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(normalizeKey(rawKey));
    }

    private byte[] normalizeKey(byte[] raw) {
        if (raw.length >= 32) {
            return raw;
        }
        byte[] padded = new byte[32];
        for (int i = 0; i < 32; i++) {
            padded[i] = raw[i % raw.length];
        }
        return padded;
    }

    public String generateToken(String subject, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpirationMinutes() * 60_000);
        return Jwts.builder()
                .subject(subject)
                .claims(Map.of("role", role))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
