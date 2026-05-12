package com.soumyajit.apigateway.service;

import com.soumyajit.apigateway.model.User;
import com.soumyajit.apigateway.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should register user successfully when details are valid")
        void register_Success() {
            // Arrange
            String username = "testuser";
            String password = "password123";
            String displayName = "Test User";
            String hashedPassword = "hashedPassword123";

            when(userRepository.existsByUsername(username)).thenReturn(false);
            when(passwordEncoder.encode(password)).thenReturn(hashedPassword);

            // REFACTORED: Manual instantiation instead of Builder
            User savedUser = new User();
            savedUser.setUsername(username);
            savedUser.setPassword(hashedPassword);
            savedUser.setDisplayName(displayName);
            savedUser.setRoles(Set.of("ROLE_USER"));

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // Act
            User result = authService.register(username, password, displayName);

            // Assert
            assertNotNull(result);
            assertEquals(username, result.getUsername());
            assertNull(result.getPassword(), "Hashed password should be cleared before returning");
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void register_UsernameExists() {
            when(userRepository.existsByUsername("existingUser")).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () ->
                    authService.register("existingUser", "password123", "Name")
            );
        }

        @Test
        @DisplayName("Should throw exception when password is too short")
        void register_ShortPassword() {
            assertThrows(IllegalArgumentException.class, () ->
                    authService.register("user", "123", "Name")
            );
        }
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should return token when credentials are valid")
        void authenticate_Success() {
            // Arrange
            String username = "user";
            String rawPassword = "password";

            // REFACTORED: Manual instantiation
            User user = new User();
            user.setUsername(username);
            user.setPassword("hashed");
            user.setRoles(Set.of("ROLE_USER"));

            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(rawPassword, "hashed")).thenReturn(true);

            // Note: Updated to match the new JwtService.generateToken signature (username, roles)
            when(jwtService.generateToken(eq(username), anyCollection())).thenReturn("mock-jwt-token");

            // Act
            String token = authService.authenticateAndGetToken(username, rawPassword);

            // Assert
            assertEquals("mock-jwt-token", token);
        }

        @Test
        @DisplayName("Should return null when password does not match")
        void authenticate_WrongPassword() {
            User user = new User();
            user.setUsername("user");
            user.setPassword("hashed");

            when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

            String token = authService.authenticateAndGetToken("user", "wrong");

            assertNull(token);
        }
    }

    @Nested
    @DisplayName("User Details Tests")
    class UserDetailsTests {

        @Test
        @DisplayName("Should load UserDetails when user exists")
        void loadUser_Success() {
            User user = new User();
            user.setUsername("user");
            user.setPassword("hashed");
            user.setRoles(Set.of("ROLE_USER", "ROLE_ADMIN"));

            when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

            UserDetails details = authService.loadUserByUsername("user");

            assertNotNull(details);
            assertEquals("user", details.getUsername());
            // Verify that multiple roles are correctly mapped to authorities
            assertTrue(details.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
            assertTrue(details.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        }

        @Test
        @DisplayName("Should return null when loading non-existent user")
        void loadUser_NotFound() {
            when(userRepository.findByUsername("none")).thenReturn(Optional.empty());

            UserDetails details = authService.loadUserByUsername("none");

            assertNull(details);
        }
    }
}