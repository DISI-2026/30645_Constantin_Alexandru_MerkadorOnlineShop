package org.example.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

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

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1h
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}

