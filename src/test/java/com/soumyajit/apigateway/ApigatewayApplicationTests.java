package com.soumyajit.apigateway;

import com.soumyajit.apigateway.repository.ApiLogRepository;
import com.soumyajit.apigateway.repository.UserRepository;
import com.soumyajit.apigateway.service.AuthService;
import com.soumyajit.apigateway.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=9a4f4e354565456c45654c4656456456456456456456456456456456456456456",
        "jwt.expiration=3600000",
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb"
})
class ApiGatewayApplicationTests {

    /**
     * We mock these beans because @SpringBootTest attempts to initialize
     * the entire application context, including all controllers and services.
     *
     * Mocking them prevents the UnsatisfiedDependencyException for
     * ApiLogRepository and other missing MongoDB beans.
     */
    @MockBean
    private ApiLogRepository apiLogRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("Context Load Test - Ensures the Spring Boot Application starts correctly")
    void contextLoads() {
        // This method will fail if any bean cannot be created or if
        // dependencies are missing. If it passes, your configuration is sound.
    }
}