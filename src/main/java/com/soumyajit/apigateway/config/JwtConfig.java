package com.soumyajit.apigateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
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
}
