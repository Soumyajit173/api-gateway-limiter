package com.soumyajit.apigateway.service;

import com.soumyajit.apigateway.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final JwtConfig jwtConfig;
    private final SecretKey key;

    private static final String ISSUER = "apigateway";
    private static final String AUDIENCE = "users";

    public JwtService(JwtConfig jwtConfig) {
        if (jwtConfig.getSecret() == null || jwtConfig.getSecret().length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters");
        }
        this.jwtConfig = jwtConfig;
        this.key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a token including the specific roles assigned to the user.
     */
    public String generateToken(String username, Collection<String> roles) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(username)
                .setIssuer(ISSUER)
                .setAudience(AUDIENCE)
                // THE FIX: Use the roles passed from the database instead of hardcoding
                .claim("roles", roles)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtConfig.getExpiration()))
                .signWith(key, SignatureAlgorithm.HS256)
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

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Extract the roles list from the claims
        return claims.get("roles", List.class);
    }
}