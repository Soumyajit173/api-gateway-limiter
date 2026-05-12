package com.soumyajit.apigateway.service;

import com.soumyajit.apigateway.config.JwtConfig;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtConfig jwtConfig;

    private JwtService jwtService;

    private final String VALID_SECRET = "super-secret-key-that-is-at-least-32-chars-long";
    private final long EXPIRATION = 3600000;

    @BeforeEach
    void setUp() {
        // Using lenient() to allow specific tests to override or ignore these stubs
        lenient().when(jwtConfig.getSecret()).thenReturn(VALID_SECRET);
        lenient().when(jwtConfig.getExpiration()).thenReturn(EXPIRATION);

        jwtService = new JwtService(jwtConfig);
    }

    @Test
    @DisplayName("Should throw exception if secret key is too short")
    void constructor_InvalidSecret() {
        JwtConfig shortConfig = mock(JwtConfig.class);
        when(shortConfig.getSecret()).thenReturn("too-short");

        assertThrows(IllegalArgumentException.class, () -> new JwtService(shortConfig));
    }

    @Test
    @DisplayName("Should generate a valid JWT token with roles")
    void generateToken_Success() {
        // Arrange
        String username = "testUser";
        Set<String> roles = Set.of("ROLE_USER", "ROLE_ADMIN");

        // Act
        String token = jwtService.generateToken(username, roles);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should validate token and return correct username")
    void validateAndGetUser_Success() {
        // Arrange
        String username = "soumyajit";
        String token = jwtService.generateToken(username, Set.of("ROLE_USER"));

        // Act
        String extractedUser = jwtService.validateAndGetUser(token);

        // Assert
        assertEquals(username, extractedUser);
    }

    @Test
    @DisplayName("Should throw exception for tampered or invalid token")
    void validateAndGetUser_InvalidToken() {
        String tamperedToken = "eyJhbGciOiJIUzI1NiJ9.invalid.payload";

        assertThrows(JwtException.class, () -> jwtService.validateAndGetUser(tamperedToken));
    }

    @Test
    @DisplayName("Should extract specific roles from token")
    void getRoles_Success() {
        // Arrange
        Set<String> expectedRoles = Set.of("ROLE_ADMIN", "ROLE_USER");
        String token = jwtService.generateToken("user123", expectedRoles);

        // Act
        List<String> roles = jwtService.getRoles(token);

        // Assert
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_ADMIN"));
        assertTrue(roles.contains("ROLE_USER"));
    }
}