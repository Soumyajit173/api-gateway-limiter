package com.soumyajit.apigateway.controller;

import com.soumyajit.apigateway.filter.JwtAuthenticationFilter;
import com.soumyajit.apigateway.filter.LoggingFilter;
import com.soumyajit.apigateway.filter.RateLimitFilter;
import com.soumyajit.apigateway.model.User;
import com.soumyajit.apigateway.repository.UserRepository;
import com.soumyajit.apigateway.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        JwtAuthenticationFilter.class,
                        RateLimitFilter.class,
                        LoggingFilter.class
                }
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("POST /auth/register - Should return user details on success")
    void register_Success() throws Exception {
        // Arrange
        AuthController.RegisterRequest req = new AuthController.RegisterRequest();
        req.setUsername("tester");
        req.setPassword("password123");
        req.setDisplayName("Test User");

        User mockUser = new User();
        mockUser.setId("user-123");
        mockUser.setUsername("tester");
        mockUser.setDisplayName("Test User");

        // FIX: Use Instant.now() directly instead of Instant.from(LocalDateTime.now())
        mockUser.setCreatedAt(Instant.now());

        // Ensure these arguments match what your Controller passes to authService.register()
        when(authService.register(anyString(), anyString(), anyString())).thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.id").value("user-123"));
    }

    @Test
    @DisplayName("POST /auth/login - Should return token on valid credentials")
    void login_Success() throws Exception {
        // Arrange
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setUsername("tester");
        req.setPassword("password123");

        when(authService.authenticateAndGetToken("tester", "password123"))
                .thenReturn("mock-jwt-token");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    @DisplayName("POST /auth/login - Should return 401 on invalid credentials")
    void login_Failure() throws Exception {
        // Arrange
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setUsername("wrong");
        req.setPassword("wrong");

        when(authService.authenticateAndGetToken(anyString(), anyString()))
                .thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid credentials"));
    }
}