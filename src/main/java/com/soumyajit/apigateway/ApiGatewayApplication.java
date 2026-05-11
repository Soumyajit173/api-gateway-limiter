package com.soumyajit.apigateway;

import com.soumyajit.apigateway.model.User;
import com.soumyajit.apigateway.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * Seeds the initial administrator user if it doesn't exist.
     * Wrapped in a try-catch to prevent startup failure if MongoDB
     * is temporarily unreachable during the deployment window.
     */
    @Bean
    CommandLineRunner seed(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            try {
                if (!repo.existsByUsername("admin")) {
                    User admin = User.builder()
                            .username("admin")
                            .password(encoder.encode("adminpass"))
                            .displayName("Administrator")
                            .createdAt(Instant.now())
                            .build();

                    repo.save(admin);
                    System.out.println(">> Database Seeded: Admin user created.");
                } else {
                    System.out.println(">> Database Check: Admin user already exists.");
                }
            } catch (Exception e) {
                // Log the error but allow the application to finish starting up.
                // This prevents the 502 Bad Gateway on Render.
                System.err.println(">> Seed Warning: Could not connect to DB during startup: " + e.getMessage());
            }
        };
    }
}