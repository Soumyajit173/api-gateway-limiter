package com.soumyajit.apigateway.controller;

import com.soumyajit.apigateway.filter.JwtAuthenticationFilter;
import com.soumyajit.apigateway.filter.LoggingFilter;
import com.soumyajit.apigateway.filter.RateLimitFilter;
import com.soumyajit.apigateway.model.User;
import com.soumyajit.apigateway.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = UserController.class,
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
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;


    @Test
    @DisplayName("GET /users/me - Should return current user when authenticated")
    void me_Authenticated() throws Exception {
        // 1. Set up your mock data
        User mockUser = new User();
        mockUser.setUsername("tester");
        mockUser.setDisplayName("Test User");
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(mockUser));

        // 2. Create the Authentication object manually
        // We use a simple String "tester" so that getName() returns exactly that.
        var auth = new UsernamePasswordAuthenticationToken("tester", null, java.util.Collections.emptyList());

        // 3. Perform the request using .with(authentication(auth))
        mockMvc.perform(get("/users/me")
                        .with(authentication(auth))) // THIS IS THE FIX
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("tester"));
    }

    @Test
    @DisplayName("GET /users/me - Should return 401 when unauthenticated")
    void me_Unauthenticated() throws Exception {
        // perform request with NO authentication object
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("unauthenticated"));
    }

    @Test
    @DisplayName("GET /users/{username} - Should return user details")
    void getByUsername_Success() throws Exception {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername("findme");
        mockUser.setDisplayName("Found User");

        when(userRepository.findByUsername("findme")).thenReturn(Optional.of(mockUser));

        // Act & Assert
        mockMvc.perform(get("/users/findme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("findme"))
                .andExpect(jsonPath("$.displayName").value("Found User"));
    }

    @Test
    @DisplayName("GET /users/{username} - Should return 404 when not found")
    void getByUsername_NotFound() throws Exception {
        // Arrange
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/users/ghost"))
                .andExpect(status().isNotFound());
    }
}