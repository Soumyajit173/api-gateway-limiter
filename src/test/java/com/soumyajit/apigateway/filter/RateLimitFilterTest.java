package com.soumyajit.apigateway.filter;

import com.soumyajit.apigateway.service.RateLimiterService;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private RateLimiterService rateLimiterService; // REFACTORED: Mock the Service

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        // Updated constructor usage
        rateLimitFilter = new RateLimitFilter(rateLimiterService);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should allow request when service returns true for IP key")
    void doFilterInternal_AllowIP() throws ServletException, IOException {
        // Arrange
        String ip = "127.0.0.1";
        when(request.getRemoteAddr()).thenReturn(ip);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Mock service to allow the request
        when(rateLimiterService.tryConsume("ip:" + ip)).thenReturn(true);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(rateLimiterService).tryConsume("ip:" + ip);
    }

    @Test
    @DisplayName("Should identify authenticated user and use username as key")
    void doFilterInternal_UseUserKey() throws ServletException, IOException {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("soumyajit");

        when(rateLimiterService.tryConsume("user:soumyajit")).thenReturn(true);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(rateLimiterService).tryConsume("user:soumyajit");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should block request and return 429 when service returns false")
    void doFilterInternal_LimitExceeded() throws ServletException, IOException {
        // Arrange
        String ip = "192.168.1.1";
        when(request.getRemoteAddr()).thenReturn(ip);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Mock service to block the request
        when(rateLimiterService.tryConsume("ip:" + ip)).thenReturn(false);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        assertTrue(stringWriter.toString().contains("Rate limit exceeded"));
        verifyNoInteractions(filterChain); // Ensure request stopped here
    }
}