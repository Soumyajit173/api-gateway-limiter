package com.soumyajit.apigateway.service;

import com.soumyajit.apigateway.model.ApiLog;
import com.soumyajit.apigateway.repository.ApiLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoggingService {
    private final ApiLogRepository repo;

    public void save(ApiLog log) {
        repo.save(log);
    }
}
