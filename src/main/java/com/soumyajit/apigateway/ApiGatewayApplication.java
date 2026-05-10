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

    @Bean
    CommandLineRunner seed(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (!repo.existsByUsername("admin")) {
                repo.save(User.builder()
                        .username("admin")
                        .password(encoder.encode("adminpass"))
                        .displayName("Administrator")
                        .createdAt(Instant.now())
                        .build());
            }
        };
    }
}
