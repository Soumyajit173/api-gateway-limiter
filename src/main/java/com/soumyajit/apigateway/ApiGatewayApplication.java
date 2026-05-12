package com.soumyajit.apigateway;

import com.soumyajit.apigateway.model.User;
import com.soumyajit.apigateway.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Set;

@SpringBootApplication
@EnableAsync // Added to support the non-blocking LoggingService we built
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * Seeds the initial administrator user if it doesn't exist.
     */
    @Bean
    CommandLineRunner seed(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            try {
                if (!repo.existsByUsername("admin")) {
                    // THE FIX: Standard POJO instantiation instead of Builder
                    User admin = new User();
                    admin.setUsername("admin");
                    admin.setPassword(encoder.encode("adminpass"));
                    admin.setDisplayName("Administrator");
                    admin.setCreatedAt(Instant.now());

                    // Critical: Give the admin proper roles
                    admin.setRoles(Set.of("ROLE_USER", "ROLE_ADMIN"));

                    repo.save(admin);
                    System.out.println(">> Database Seeded: Admin user created with ROLE_ADMIN.");
                } else {
                    System.out.println(">> Database Check: Admin user already exists.");
                }
            } catch (Exception e) {
                // Log the error but allow application startup to continue
                System.err.println(">> Seed Warning: Could not connect to DB during startup: " + e.getMessage());
            }
        };
    }
}