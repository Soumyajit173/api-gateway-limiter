package com.soumyajit.apigateway.filter;

import com.soumyajit.apigateway.model.ApiLog;
import com.soumyajit.apigateway.repository.ApiLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    @Mock
    private ApiLogRepository apiLogRepository;

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

    private LoggingFilter loggingFilter;

    @BeforeEach
    void setUp() {
        loggingFilter = new LoggingFilter(apiLogRepository);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should capture and save log details after request completes")
    void doFilterInternal_ShouldSaveLog() throws ServletException, IOException {
        // Arrange
        String path = "/api/resource";
        String method = "POST";
        String clientIp = "192.168.1.1";
        int status = 201;

        when(request.getRequestURI()).thenReturn(path);
        when(request.getMethod()).thenReturn(method);
        when(request.getRemoteAddr()).thenReturn(clientIp);

        // FIX: Stub X-Forwarded-For to return null so it falls back to RemoteAddr
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        when(request.getHeaderNames()).thenReturn(Collections.enumeration(List.of("User-Agent")));
        when(request.getHeader("User-Agent")).thenReturn("JUnit-Test");

        when(response.getStatus()).thenReturn(status);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test-user");

        // Act
        loggingFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        ArgumentCaptor<ApiLog> logCaptor = ArgumentCaptor.forClass(ApiLog.class);
        verify(apiLogRepository).save(logCaptor.capture());

        ApiLog capturedLog = logCaptor.getValue();
        assertEquals(path, capturedLog.getPath());
        assertEquals("192.168.1.1", capturedLog.getClientIp());
    }

    @Test
    @DisplayName("Should extract IP from X-Forwarded-For header if present")
    void getClientIp_ShouldUseForwardedHeader() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.5, 127.0.0.1");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());

        // Act
        loggingFilter.doFilterInternal(request, response, filterChain);

        // Assert
        ArgumentCaptor<ApiLog> logCaptor = ArgumentCaptor.forClass(ApiLog.class);
        verify(apiLogRepository).save(logCaptor.capture());
        assertEquals("10.0.0.5", logCaptor.getValue().getClientIp());
    }

    @Test
    @DisplayName("Should handle repository failure gracefully without crashing the request")
    void doFilterInternal_ShouldHandleSaveException() throws ServletException, IOException {
        // Arrange
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        doThrow(new RuntimeException("DB Down")).when(apiLogRepository).save(any(ApiLog.class));

        // Act & Assert
        // The filter should NOT throw an exception because of the try-catch block
        assertDoesNotThrow(() -> loggingFilter.doFilterInternal(request, response, filterChain));

        // Ensure the request still went through
        verify(filterChain).doFilter(request, response);
    }
}