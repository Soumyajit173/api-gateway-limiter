package com.soumyajit.apigateway.service;

import com.soumyajit.apigateway.model.RateLimitCounter;
import com.soumyajit.apigateway.repository.RateLimitRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class RateLimiterService {

    private final RateLimitRepository repo;
    private final long capacity;
    private final long refillRate;

    // Custom constructor for config injection
    public RateLimiterService(RateLimitRepository repo, long capacity, long refillRate) {
        this.repo = repo;
        this.capacity = capacity;
        this.refillRate = refillRate;
    }

    public boolean tryConsume(String key) {
        Optional<RateLimitCounter> opt = repo.findByKey(key);
        RateLimitCounter counter = opt.orElseGet(() -> RateLimitCounter.builder()
                .key(key)
                .tokens(capacity)
                .capacity(capacity)
                .lastRefill(Instant.now())
                .build());

        refill(counter);

        if (counter.getTokens() > 0) {
            counter.setTokens(counter.getTokens() - 1);
            repo.save(counter);
            return true;
        }

        repo.save(counter);
        return false;
    }

    private void refill(RateLimitCounter counter) {
        long elapsedSeconds = (Instant.now().getEpochSecond() - counter.getLastRefill().getEpochSecond());
        long tokensToAdd = elapsedSeconds * refillRate;
        if (tokensToAdd > 0) {
            counter.setTokens(Math.min(counter.getCapacity(), counter.getTokens() + tokensToAdd));
            counter.setLastRefill(Instant.now());
        }
    }
}
