package com.soumyajit.apigateway.config;

import com.soumyajit.apigateway.repository.RateLimitRepository;
import com.soumyajit.apigateway.service.RateLimiterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {

    @Value("${ratelimit.capacity:10}")
    private long capacity;

    @Value("${ratelimit.refillRate:1}")
    private long refillRate;

    @Bean
    public RateLimiterService rateLimiterService(RateLimitRepository repo) {
        // Construct the service with values injected from application.yml
        return new RateLimiterService(repo, capacity, refillRate);
    }
}
