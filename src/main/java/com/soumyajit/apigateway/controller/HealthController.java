package com.soumyajit.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok().body(java.util.Map.of(
                "status", "UP",
                "service", "api-gateway",
                "timestamp", java.time.Instant.now().toString()
        ));
    }
}
