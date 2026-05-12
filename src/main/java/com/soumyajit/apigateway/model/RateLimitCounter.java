package com.soumyajit.apigateway.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "rate_limits")
public class RateLimitCounter {
    @Id
    private String id;

    private String key;
    private long tokens;
    private long capacity;
    private Instant lastRefill;

    public RateLimitCounter(String id, String key, long tokens, long capacity, Instant lastRefill) {
        this.id = id;
        this.key = key;
        this.tokens = tokens;
        this.capacity = capacity;
        this.lastRefill = lastRefill;
    }

    public RateLimitCounter() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getTokens() {
        return tokens;
    }

    public void setTokens(long tokens) {
        this.tokens = tokens;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public Instant getLastRefill() {
        return lastRefill;
    }

    public void setLastRefill(Instant lastRefill) {
        this.lastRefill = lastRefill;
    }
}
