package com.soumyajit.apigateway.service;

import com.soumyajit.apigateway.model.User;
import com.soumyajit.apigateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


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
        User u = User.builder()
                .username(username)
                .password(hashed)
                .displayName(displayName)
                .createdAt(Instant.now())
                .build();

        User saved = userRepository.save(u);
        saved.setPassword(null); // do not expose hashed password
        return saved;
    }


    public String authenticateAndGetToken(String username, String rawPassword) {
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) return null;
        User u = opt.get();
        if (!passwordEncoder.matches(rawPassword, u.getPassword())) return null;
        return jwtService.generateToken(u.getUsername());
    }

    public UserDetails loadUserByUsername(String username) {
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) return null;
        User u = opt.get();
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUsername())
                .password(u.getPassword())
                .authorities("USER")
                .build();
    }

}
