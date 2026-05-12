package com.soumyajit.apigateway.filter;

import com.soumyajit.apigateway.model.ApiLog;
import com.soumyajit.apigateway.service.LoggingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    private final LoggingService loggingService;

    // Injecting the Service instead of the Repository for cleaner architecture
    public LoggingFilter(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long start = System.currentTimeMillis();

        // 1. Let the request proceed
        filterChain.doFilter(request, response);

        // 2. Calculate metrics after the response is generated
        long duration = System.currentTimeMillis() - start;

        // 3. THE FIX: Standard POJO instantiation instead of Builder
        ApiLog log = new ApiLog();
        log.setTimestamp(Instant.now());
        log.setMethod(request.getMethod());
        log.setPath(request.getRequestURI());
        log.setClientIp(getClientIp(request));
        log.setUsername(getUsername());
        log.setStatus(response.getStatus());
        log.setDurationMs(duration);
        log.setHeaders(extractHeaders(request));

        // 4. Delegate saving (preferably using the @Async save we configured)
        loggingService.save(log);
    }

    private String getClientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private String getUsername() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return "anonymous";
        return auth.getName();
    }

    private Map<String, String> extractHeaders(HttpServletRequest req) {
        return Collections.list(req.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(
                        h -> h,
                        req::getHeader,
                        (existing, replacement) -> existing // Handle duplicate headers if any
                ));
    }
}