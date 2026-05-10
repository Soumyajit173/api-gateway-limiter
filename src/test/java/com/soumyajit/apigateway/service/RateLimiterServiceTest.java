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
        // Initializing manually because of the custom constructor
        rateLimiterService = new RateLimiterService(repo, CAPACITY, REFILL_RATE);
    }

    @Test
    @DisplayName("Should allow request and consume token when bucket is full")
    void tryConsume_Success() {
        // Arrange: No existing counter in DB (new user)
        when(repo.findByKey(KEY)).thenReturn(Optional.empty());

        // Act
        boolean allowed = rateLimiterService.tryConsume(KEY);

        // Assert
        assertTrue(allowed);
        ArgumentCaptor<RateLimitCounter> captor = ArgumentCaptor.forClass(RateLimitCounter.class);
        verify(repo).save(captor.capture());

        // Tokens should be CAPACITY - 1
        assertEquals(CAPACITY - 1, captor.getValue().getTokens());
    }

    @Test
    @DisplayName("Should reject request when tokens are exhausted")
    void tryConsume_Exhausted() {
        // Arrange: Existing counter with 0 tokens and recent refill
        RateLimitCounter counter = RateLimitCounter.builder()
                .key(KEY)
                .tokens(0)
                .capacity(CAPACITY)
                .lastRefill(Instant.now())
                .build();

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
        // With REFILL_RATE = 1, it should add 5 tokens
        Instant fiveSecondsAgo = Instant.now().minusSeconds(5);
        RateLimitCounter counter = RateLimitCounter.builder()
                .key(KEY)
                .tokens(0)
                .capacity(CAPACITY)
                .lastRefill(fiveSecondsAgo)
                .build();

        when(repo.findByKey(KEY)).thenReturn(Optional.of(counter));

        // Act
        boolean allowed = rateLimiterService.tryConsume(KEY);

        // Assert
        assertTrue(allowed);
        // Calculation: 0 (start) + 5 (refill) - 1 (consumed) = 4
        assertEquals(4, counter.getTokens());
        verify(repo).save(counter);
    }

    @Test
    @DisplayName("Should not exceed maximum capacity during refill")
    void tryConsume_CapAtCapacity() {
        // Arrange: 9 tokens, last refill 1 hour ago
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        RateLimitCounter counter = RateLimitCounter.builder()
                .key(KEY)
                .tokens(9)
                .capacity(CAPACITY)
                .lastRefill(oneHourAgo)
                .build();

        when(repo.findByKey(KEY)).thenReturn(Optional.of(counter));

        // Act
        rateLimiterService.tryConsume(KEY);

        // Assert
        // Calculation: 9 + 3600 is way over CAPACITY(10).
        // Should cap at 10, then consume 1 to result in 9.
        assertEquals(9, counter.getTokens());
    }
}