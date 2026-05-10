package com.soumyajit.apigateway.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle generic Exception and return 500 Internal Server Error")
    void handleAll_ReturnsInternalServerError() {
        // Arrange
        String message = "Unexpected system error";
        String uri = "/api/test";
        Exception ex = new Exception(message);
        when(request.getRequestURI()).thenReturn(uri);

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleAll(ex, request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(message, response.getBody().getMessage());
        assertEquals(uri, response.getBody().getPath());
        assertEquals(500, response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException and return 400 Bad Request")
    void handleBadRequest_ReturnsBadRequest() {
        // Arrange
        String message = "Invalid input provided";
        String uri = "/api/resource";
        IllegalArgumentException ex = new IllegalArgumentException(message);
        when(request.getRequestURI()).thenReturn(uri);

        // Act
        ResponseEntity<ApiError> response = exceptionHandler.handleBadRequest(ex, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response.getBody().getError());
        assertEquals(message, response.getBody().getMessage());
        assertEquals(uri, response.getBody().getPath());
    }
}