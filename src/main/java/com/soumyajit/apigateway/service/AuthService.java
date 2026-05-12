package com.soumyajit.apigateway.service;

import com.soumyajit.apigateway.model.User;
import com.soumyajit.apigateway.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String username, String rawPassword, String displayName) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username required");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("password must be at least 6 characters");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("username already exists");
        }

        String hashed = passwordEncoder.encode(rawPassword);

        // REFACTORED: Using standard constructor instead of Builder
        User u = new User();
        u.setUsername(username);
        u.setPassword(hashed);
        u.setDisplayName(displayName);
        u.setCreatedAt(Instant.now());
        // Fix for the 500 error: Ensure roles are initialized
        u.setRoles(Set.of("ROLE_USER"));

        User saved = userRepository.save(u);
        saved.setPassword(null);
        return saved;
    }

    public String authenticateAndGetToken(String username, String rawPassword) {
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) return null;

        User u = opt.get();
        if (!passwordEncoder.matches(rawPassword, u.getPassword())) return null;

        // Pass roles to the token generator so they are included in the JWT
        return jwtService.generateToken(u.getUsername(), u.getRoles());
    }

    public UserDetails loadUserByUsername(String username) {
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) return null;

        User u = opt.get();

        // Handle potential null roles from older database records
        Set<String> roles = u.getRoles() != null ? u.getRoles() : Collections.emptySet();

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUsername())
                .password(u.getPassword())
                .authorities(roles.stream()
                        .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                        .toList())
                .build();
    }
}