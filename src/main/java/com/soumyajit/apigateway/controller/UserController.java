package com.soumyajit.apigateway.controller;

import com.soumyajit.apigateway.model.User;
import com.soumyajit.apigateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "unauthenticated"));
        }

        String username = authentication.getName();
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(java.util.Map.of("error", "user not found"));
        }

        User u = opt.get();
        u.setPassword(null);
        return ResponseEntity.ok(u);
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getByUsername(@PathVariable String username) {
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        User u = opt.get();
        u.setPassword(null);
        return ResponseEntity.ok(u);
    }
}
