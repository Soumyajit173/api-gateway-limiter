package com.soumyajit.apigateway.controller;

import com.soumyajit.apigateway.model.User;
import com.soumyajit.apigateway.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        User created = authService.register(req.getUsername(), req.getPassword(), req.getDisplayName());
        return ResponseEntity.ok(Map.of(
                "id", created.getId(),
                "username", created.getUsername(),
                "displayName", created.getDisplayName(),
                "createdAt", created.getCreatedAt()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        String token = authService.authenticateAndGetToken(req.getUsername(), req.getPassword());
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid credentials"));
        }
        return ResponseEntity.ok(Map.of("token", token));
    }

    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String displayName;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
