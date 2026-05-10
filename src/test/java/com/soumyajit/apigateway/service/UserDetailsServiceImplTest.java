package com.soumyajit.apigateway.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Should return UserDetails when AuthService finds the user")
    void loadUserByUsername_Success() {
        // Arrange
        String username = "soumyajit";
        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getUsername()).thenReturn(username);
        when(authService.loadUserByUsername(username)).thenReturn(mockUserDetails);

        // Act
        UserDetails result = userDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(authService, times(1)).loadUserByUsername(username);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when AuthService returns null")
    void loadUserByUsername_UserNotFound() {
        // Arrange
        String username = "unknown_user";
        when(authService.loadUserByUsername(username)).thenReturn(null);

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(username)
        );

        assertTrue(exception.getMessage().contains(username));
        verify(authService, times(1)).loadUserByUsername(username);
    }
}