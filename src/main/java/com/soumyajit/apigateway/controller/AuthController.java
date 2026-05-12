package com.soumyajit.apigateway.controller;

import com.soumyajit.apigateway.model.User;
import com.soumyajit.apigateway.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

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

    // --- REFACTORED INNER CLASSES (Removed Lombok) ---

    public static class RegisterRequest {
        private String username;
        private String password;
        private String displayName;

        public RegisterRequest() {}

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest() {}

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}