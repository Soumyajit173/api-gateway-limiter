package com.soumyajit.apigateway.service;

import com.soumyajit.apigateway.model.RateLimitCounter;
import com.soumyajit.apigateway.repository.RateLimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterService {

    private final RateLimitRepository repo;

    @Value("${ratelimit.capacity:100}")
    private double capacity;

    @Value("${ratelimit.refillPerMinute:100}")
    private double refillPerMinute;


    public synchronized boolean tryConsume(String key, int tokensToConsume) {
        Optional<RateLimitCounter> opt = repo.findById(key);
        RateLimitCounter counter = opt.orElseGet(() -> RateLimitCounter.builder()
                .id(key)
                .tokens((long) capacity)
                .lastRefill(Instant.now())
                .build());

        refill(counter);

        if (counter.getTokens() >= tokensToConsume) {
            counter.setTokens(counter.getTokens() - tokensToConsume);
            repo.save(counter);
            return true;
        } else {
            repo.save(counter);
            return false;
        }
    }

    private void refill(RateLimitCounter counter) {
        Instant now = Instant.now();
        long seconds = Duration.between(counter.getLastRefill(), now).getSeconds();
        if (seconds <= 0) return;
        double tokensToAdd = (refillPerMinute / 60.0) * seconds;
        double newTokens = Math.min(capacity, counter.getTokens() + tokensToAdd);
        counter.setTokens((long) newTokens);
        counter.setLastRefill(now);
    }
}
