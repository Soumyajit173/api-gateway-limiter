package com.soumyajit.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/gateway")
public class GatewayController {

    @GetMapping("/hello")
    public ResponseEntity<?> hello(Authentication authentication) {
        String user = authentication != null ? authentication.getName() : "anonymous";
        return ResponseEntity.ok(Map.of(
                "message", "Hello from API Gateway",
                "user", user,
                "timestamp", Instant.now().toString()
        ));
    }

    @PostMapping("/echo")
    public ResponseEntity<?> echo(@RequestBody Map<String, Object> body, Authentication authentication) {
        String user = authentication != null ? authentication.getName() : "anonymous";
        return ResponseEntity.ok(Map.of(
                "echo", body,
                "user", user,
                "receivedAt", Instant.now().toString()
        ));
    }

    @GetMapping("/echo-headers")
    public ResponseEntity<?> echoHeaders(@RequestHeader Map<String, String> headers, Authentication authentication) {
        String user = authentication != null ? authentication.getName() : "anonymous";
        return ResponseEntity.ok(Map.of(
                "headers", headers,
                "user", user,
                "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/trigger-error")
    public ResponseEntity<?> triggerError() {
        throw new RuntimeException("Intentional error for testing GlobalExceptionHandler");
    }
}
