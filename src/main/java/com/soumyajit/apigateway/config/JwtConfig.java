package com.soumyajit.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * The secret key used to sign JWT tokens.
     * Configure in application.yml as: jwt.secret=yourSecretKey
     */
    private String secret;

    /**
     * Token expiration time in milliseconds.
     * Configure in application.yml as: jwt.expiration=3600000 (1 hour)
     */
    private long expiration;

    // Standard no-args constructor for Spring configuration binding
    public JwtConfig() {
    }

    public JwtConfig(String secret, long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}