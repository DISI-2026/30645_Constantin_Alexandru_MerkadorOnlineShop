package org.example.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final String secretEnv;

    private SecretKey key;

    // Inject value from env variable (or default for local dev)
    public JwtService(@Value("${JWT_SECRET:local-secret-demo-key-change-me!}") String secretEnv) {
        this.secretEnv = secretEnv;
    }

    @PostConstruct
    private void init() {
        if (secretEnv == null || secretEnv.isBlank()) {
            throw new IllegalStateException("JWT secret is empty");
        }
        this.key = Keys.hmacShaKeyFor(secretEnv.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String activeRole, List<String> roles) {
        return Jwts.builder()
                .claim("activeRole", activeRole)
                .claim("roles", roles)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1h
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractActiveRole(String token) {
        return parseClaims(token).get("activeRole").toString();
    }

    public List<String> extractRoles(String token) {
        return (List<String>) parseClaims(token).get("roles");
    }

    // Private helper — parses and validates the token in one shot.
    // Throws ExpiredJwtException, MalformedJwtException, etc. on failure.
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

