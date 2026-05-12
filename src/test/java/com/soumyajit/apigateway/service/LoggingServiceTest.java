package com.soumyajit.apigateway.service;

import com.soumyajit.apigateway.model.ApiLog;
import com.soumyajit.apigateway.repository.ApiLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingServiceTest {

    @Mock
    private ApiLogRepository repo;

    @InjectMocks
    private LoggingService loggingService;

    @Test
    @DisplayName("Should successfully delegate log saving to the repository")
    void save_Success() {
        // Arrange
        // REFACTORED: Manual instantiation instead of Builder
        ApiLog log = new ApiLog();
        log.setPath("/api/test");
        log.setMethod("GET");
        log.setStatus(200);
        log.setClientIp("127.0.0.1");
        log.setTimestamp(Instant.now());

        // Act
        loggingService.save(log);

        // Assert
        // Verify that repo.save was called exactly once with our log object
        verify(repo, times(1)).save(log);
    }
}