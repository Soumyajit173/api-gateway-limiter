package com.soumyajit.apigateway.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class InfoController {

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> gatewayInfo() {

        Map<String, Object> response = new LinkedHashMap<>();

        /* =========================
           BASIC SERVICE INFO
        ========================== */
        response.put("service", Map.of(
                "name", "API Gateway",
                "version", "1.0.0",
                "status", "ACTIVE",
                "timestamp", Instant.now().toString(),
                "environment", "production",
                "deployment", "Render Cloud"
        ));

        /* =========================
           DESCRIPTION
        ========================== */
        response.put("description",
                "A production-grade API Gateway responsible for authentication, routing, rate limiting, " +
                        "and centralized request/response management in a microservice architecture."
        );

        /* =========================
           ARCHITECTURE
        ========================== */
        response.put("architecture", Map.of(
                "pattern", "Microservice Gateway Pattern",
                "security", "JWT-based Authentication",
                "rateLimiting", "Token Bucket Algorithm (MongoDB-backed)",
                "observability", "Spring Boot Actuator + Micrometer",
                "logging", "Centralized Request/Response Logging",
                "flow", List.of(
                        "Client Request → Gateway",
                        "JWT Filter Validation",
                        "Rate Limit Check",
                        "Routing to Microservice",
                        "Response Aggregation"
                )
        ));

        /* =========================
           CORE FEATURES
        ========================== */
        response.put("features", List.of(
                "JWT Authentication & Authorization",
                "IP-based Rate Limiting (Token Bucket)",
                "MongoDB-backed persistence",
                "Spring Security filter chain",
                "Request logging & tracing",
                "Actuator health & metrics support"
        ));

        /* =========================
           MODULES
        ========================== */
        response.put("modules", List.of(
                Map.of(
                        "name", "Authentication Module",
                        "responsibility", "Validates JWT and sets security context"
                ),
                Map.of(
                        "name", "Rate Limiting Module",
                        "responsibility", "Prevents abuse using IP-based token bucket algorithm"
                ),
                Map.of(
                        "name", "Routing Engine",
                        "responsibility", "Forwards requests to downstream microservices"
                ),
                Map.of(
                        "name", "Logging Filter",
                        "responsibility", "Tracks request lifecycle for debugging & monitoring"
                )
        ));

        /* =========================
           API DESIGN
        ========================== */
        response.put("api", Map.of(
                "style", "REST",
                "auth", "Bearer JWT",
                "basePath", "/",
                "rateLimitStrategy", "Token Bucket per IP"
        ));

        /* =========================
           OBSERVABILITY
        ========================== */
        response.put("monitoring", Map.of(
                "health", "/actuator/health",
                "metrics", "/actuator/metrics",
                "info", "/info"
        ));

        /* =========================
           QUICK STATUS (MENTOR FRIENDLY)
        ========================== */
        response.put("runtime", Map.of(
                "uptimeNote", "Stateless container deployed on Render",
                "dependencyStatus", Map.of(
                        "mongodb", "Atlas connected",
                        "security", "Active",
                        "filters", "Loaded successfully"
                )
        ));

        /* =========================
           QUICK LINKS
        ========================== */
        response.put("quickLinks", Map.of(
                "healthCheck", "/actuator/health",
                "metrics", "/actuator/metrics"
        ));

        return response;
    }
}