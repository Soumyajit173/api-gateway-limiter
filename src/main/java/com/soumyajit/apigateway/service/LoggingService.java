package com.soumyajit.apigateway.service;

import com.soumyajit.apigateway.model.ApiLog;
import com.soumyajit.apigateway.repository.ApiLogRepository;
import org.springframework.stereotype.Service;

@Service
// REFACTORED: Removed @RequiredArgsConstructor
public class LoggingService {

    private final ApiLogRepository repo;

    // Manually defined constructor for dependency injection
    public LoggingService(ApiLogRepository repo) {
        this.repo = repo;
    }

    public void save(ApiLog log) {
        repo.save(log);
    }
}