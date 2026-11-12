package org.example.spring_project.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24h

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UUID userId, String login, String role, String lastname, String firstname, String middlename, String tenantId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("login", login)
                .claim("role", role)
                .claim("lastname", lastname)
                .claim("firstname", firstname)
                .claim("middlename", middlename)
                .claim("tenantId", tenantId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(secretKey)
                .compact();
    }

    public Claims verifyToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
