package com.soumyajit.apigateway.filter;

import com.soumyajit.apigateway.model.RateLimitCounter;
import com.soumyajit.apigateway.repository.RateLimitRepository;
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
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private RateLimitRepository rateLimitRepository;

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

    private final long CAPACITY = 5;
    private final long REFILL_RATE = 1;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter(rateLimitRepository, CAPACITY, REFILL_RATE);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should allow request when tokens are available for an IP-based key")
    void doFilterInternal_AllowIP() throws ServletException, IOException {
        // Arrange
        String ip = "127.0.0.1";
        when(securityContext.getAuthentication()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(ip);
        when(rateLimitRepository.findByKey("ip:" + ip)).thenReturn(Optional.empty());

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(rateLimitRepository).save(any(RateLimitCounter.class));
    }

    @Test
    @DisplayName("Should use username as key when authenticated")
    void doFilterInternal_UseUserKey() throws ServletException, IOException {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("soumyajit");
        when(rateLimitRepository.findByKey("user:soumyajit")).thenReturn(Optional.empty());

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(rateLimitRepository).save(argThat(counter -> counter.getKey().equals("user:soumyajit")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should block request and return 429 when rate limit is exceeded")
    void doFilterInternal_LimitExceeded() throws ServletException, IOException {
        // Arrange
        String ip = "192.168.1.1";
        when(securityContext.getAuthentication()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(ip);

        RateLimitCounter exhaustedCounter = RateLimitCounter.builder()
                .key("ip:" + ip)
                .tokens(0)
                .capacity(CAPACITY)
                .lastRefill(Instant.now())
                .build();

        when(rateLimitRepository.findByKey("ip:" + ip)).thenReturn(Optional.of(exhaustedCounter));

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        assertEquals("Rate limit exceeded", stringWriter.toString());
        verifyNoInteractions(filterChain);
    }

    @Test
    @DisplayName("Should refill tokens if sufficient time has passed")
    void doFilterInternal_RefillTokens() throws ServletException, IOException {
        // Arrange
        String ip = "1.1.1.1";
        when(securityContext.getAuthentication()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(ip);

        // Last refill 2 minutes ago, refill rate 1 per minute. 0 + 2 = 2 tokens.
        RateLimitCounter counter = RateLimitCounter.builder()
                .key("ip:" + ip)
                .tokens(0)
                .capacity(CAPACITY)
                .lastRefill(Instant.now().minus(java.time.Duration.ofMinutes(2)))
                .build();

        when(rateLimitRepository.findByKey("ip:" + ip)).thenReturn(Optional.of(counter));

        // Act
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        // Assert
        // Start: 0, Refill: +2, Consume: -1 -> Result: 1
        verify(rateLimitRepository).save(argThat(c -> c.getTokens() == 1));
        verify(filterChain).doFilter(request, response);
    }
}