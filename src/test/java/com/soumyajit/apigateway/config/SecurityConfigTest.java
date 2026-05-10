package com.soumyajit.apigateway.config;

import com.soumyajit.apigateway.filter.JwtAuthenticationFilter;
import com.soumyajit.apigateway.filter.LoggingFilter;
import com.soumyajit.apigateway.filter.RateLimitFilter;
import com.soumyajit.apigateway.repository.UserRepository;
import com.soumyajit.apigateway.service.AuthService;
import com.soumyajit.apigateway.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "jwt.secret=9a4f4e354565456c45654c4656456456456456456456456456456456456456456",
        "jwt.expiration=3600000"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // We MUST mock the filters because SecurityConfig injects them into the constructor.
    // If these aren't beans in the context, SecurityConfig won't initialize.
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private LoggingFilter loggingFilter;

    // These prevent the "UnsatisfiedDependencyException" you saw in your logs.
    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtConfig jwtConfig;

    @Test
    @DisplayName("Public path /health should return 200 OK")
    void publicPath_Health_ShouldBeOpen() throws Exception {
        // Since /health is permitAll(), it should bypass security filters
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public path /auth/** should not return 401")
    void publicPath_Auth_ShouldBeOpen() throws Exception {
        // Even if the controller doesn't exist, we want 404, NOT 401.
        // 401 means security BLOCKED it. 404 means security ALLOWED it but the path is empty.
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Secure path should return 401 Unauthorized")
    void securePath_ShouldBeProtected() throws Exception {
        // This path is NOT in your permitAll list.
        // Your restAuthenticationEntryPoint should return 401.
        mockMvc.perform(get("/any-secure-api/data"))
                .andExpect(status().isUnauthorized());
    }
}