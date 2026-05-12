package com.soumyajit.apigateway.service;

import com.soumyajit.apigateway.model.RateLimitCounter;
import com.soumyajit.apigateway.repository.RateLimitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private RateLimitRepository repo;

    private RateLimiterService rateLimiterService;

    private final long CAPACITY = 10;
    private final long REFILL_RATE = 1; // 1 token per second
    private final String KEY = "test-api-key";

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService(repo, CAPACITY, REFILL_RATE);
    }

    @Test
    @DisplayName("Should allow request and consume token when bucket is full")
    void tryConsume_Success() {
        // Arrange
        when(repo.findByKey(KEY)).thenReturn(Optional.empty());

        // Act
        boolean allowed = rateLimiterService.tryConsume(KEY);

        // Assert
        assertTrue(allowed);
        ArgumentCaptor<RateLimitCounter> captor = ArgumentCaptor.forClass(RateLimitCounter.class);
        verify(repo).save(captor.capture());

        assertEquals(CAPACITY - 1, captor.getValue().getTokens());
    }

    @Test
    @DisplayName("Should reject request when tokens are exhausted")
    void tryConsume_Exhausted() {
        // Arrange
        RateLimitCounter counter = new RateLimitCounter();
        counter.setKey(KEY);
        counter.setTokens(0);
        counter.setCapacity(CAPACITY);
        counter.setLastRefill(Instant.now());

        when(repo.findByKey(KEY)).thenReturn(Optional.of(counter));

        // Act
        boolean allowed = rateLimiterService.tryConsume(KEY);

        // Assert
        assertFalse(allowed);
        verify(repo).save(counter);
        assertEquals(0, counter.getTokens());
    }

    @Test
    @DisplayName("Should refill tokens based on elapsed time")
    void tryConsume_WithRefill() {
        // Arrange: 0 tokens, but last refill was 5 seconds ago
        Instant fiveSecondsAgo = Instant.now().minusSeconds(5);

        RateLimitCounter counter = new RateLimitCounter();
        counter.setKey(KEY);
        counter.setTokens(0);
        counter.setCapacity(CAPACITY);
        counter.setLastRefill(fiveSecondsAgo);

        when(repo.findByKey(KEY)).thenReturn(Optional.of(counter));

        // Act
        boolean allowed = rateLimiterService.tryConsume(KEY);

        // Assert
        assertTrue(allowed);
        // Logic: 0 (initial) + 5 (refilled) - 1 (consumed) = 4
        assertEquals(4, counter.getTokens());
    }

    @Test
    @DisplayName("Should not exceed maximum capacity during refill")
    void tryConsume_CapAtCapacity() {
        // Arrange: 9 tokens, last refill 1 hour ago
        Instant oneHourAgo = Instant.now().minusSeconds(3600);

        RateLimitCounter counter = new RateLimitCounter();
        counter.setKey(KEY);
        counter.setTokens(9);
        counter.setCapacity(CAPACITY);
        counter.setLastRefill(oneHourAgo);

        when(repo.findByKey(KEY)).thenReturn(Optional.of(counter));

        // Act
        rateLimiterService.tryConsume(KEY);

        // Assert
        // Logic: (9 + 3600) capped at 10, then -1 consumed = 9
        assertEquals(9, counter.getTokens());
    }
}