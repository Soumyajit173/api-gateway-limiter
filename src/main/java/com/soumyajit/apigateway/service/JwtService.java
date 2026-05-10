package com.soumyajit.apigateway.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    private static final String ISSUER = "apigateway";
    private static final String AUDIENCE = "users";

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.exp-ms:3600000}") long expirationMs) {

        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters");
        }

        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(username)
                .setIssuer(ISSUER)
                .setAudience(AUDIENCE)
                .claim("roles", List.of("ROLE_USER")) // extend later
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(key)
                .compact();
    }

    public String validateAndGetUser(String token) throws JwtException {
        Claims claims = Jwts.parserBuilder()
                .requireIssuer(ISSUER)
                .requireAudience(AUDIENCE)
                .setAllowedClockSkewSeconds(30)
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public List<String> getRoles(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("roles", List.class);
    }
}