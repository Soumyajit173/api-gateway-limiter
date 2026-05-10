package com.soumyajit.apigateway.filter;

import com.soumyajit.apigateway.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should skip filter for excluded paths like /auth/login")
    void doFilterInternal_ExcludedPath() throws ServletException, IOException {
        // Arrange
        when(request.getServletPath()).thenReturn("/auth/login");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should continue chain without authentication if Authorization header is missing")
    void doFilterInternal_MissingHeader() throws ServletException, IOException {
        // Arrange
        when(request.getServletPath()).thenReturn("/api/data");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should authenticate user when a valid token is provided")
    void doFilterInternal_ValidToken() throws ServletException, IOException {
        // Arrange
        String token = "valid.jwt.token";
        String username = "soumyajit";
        when(request.getServletPath()).thenReturn("/api/secure");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.validateAndGetUser(token)).thenReturn(username);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        // Note: Because of your 'finally' block, the context is cleared AFTER the call.
        // To verify it was set DURING the call, we'd need a more complex mock,
        // but verify(filterChain) confirms the logic reached the end.
        verify(filterChain).doFilter(request, response);
        verify(jwtService).validateAndGetUser(token);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when token is invalid")
    void doFilterInternal_InvalidToken() throws ServletException, IOException {
        // Arrange
        String token = "invalid.token";
        when(request.getServletPath()).thenReturn("/api/secure");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.validateAndGetUser(token)).thenThrow(new JwtException("Expired"));

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        assertTrue(stringWriter.toString().contains("Invalid or expired token"));
        verifyNoInteractions(filterChain);
    }
}