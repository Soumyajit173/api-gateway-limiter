package com.soumyajit.apigateway.controller;

import com.soumyajit.apigateway.filter.JwtAuthenticationFilter;
import com.soumyajit.apigateway.filter.LoggingFilter;
import com.soumyajit.apigateway.filter.RateLimitFilter;
import com.soumyajit.apigateway.repository.UserRepository;
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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = GatewayController.class,
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
class GatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // --- Infrastructure Mocks (Required to bootstrap the context) ---
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;
    // ---------------------------------------------------------------

    @Test
    @DisplayName("GET /gateway/hello - Should return greeting and user info")
    void hello_Success() throws Exception {
        mockMvc.perform(get("/gateway/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello from API Gateway"))
                .andExpect(jsonPath("$.user").exists());
    }

    @Test
    @DisplayName("POST /gateway/echo - Should echo the request body")
    void echo_Success() throws Exception {
        String jsonBody = "{\"key\": \"value\", \"number\": 123}";

        mockMvc.perform(post("/gateway/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.echo.key").value("value"))
                .andExpect(jsonPath("$.echo.number").value(123));
    }

    @Test
    @DisplayName("GET /gateway/echo-headers - Should return request headers")
    void echoHeaders_Success() throws Exception {
        mockMvc.perform(get("/gateway/echo-headers")
                        .header("X-Custom-Header", "TestValue")) // Sent as Mixed-Case
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headers['X-Custom-Header']").value("TestValue")); // Match Mixed-Case
    }

    @Test
    @DisplayName("GET /gateway/trigger-error - Should result in server error")
    void triggerError_Behavior() throws Exception {
        // Since this method throws a RuntimeException, MockMvc will see it
        // as a 500 status (or whatever your GlobalExceptionHandler maps it to).
        mockMvc.perform(get("/gateway/trigger-error"))
                .andExpect(status().isInternalServerError());
    }
}