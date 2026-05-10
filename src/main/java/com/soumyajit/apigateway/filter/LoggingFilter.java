package com.soumyajit.apigateway.filter;

import com.soumyajit.apigateway.model.ApiLog;
import com.soumyajit.apigateway.repository.ApiLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    private final ApiLogRepository apiLogRepository;

    public LoggingFilter(ApiLogRepository apiLogRepository) {
        this.apiLogRepository = apiLogRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long start = System.currentTimeMillis();

        filterChain.doFilter(request, response);
        long duration = System.currentTimeMillis() - start;

        ApiLog log = ApiLog.builder()
                .timestamp(Instant.now())
                .method(request.getMethod())
                .path(request.getRequestURI())
                .clientIp(getClientIp(request))
                .username(getUsername())
                .status(response.getStatus())
                .durationMs(duration)
                .headers(extractHeaders(request))
                .build();

        try {
            apiLogRepository.save(log);
        } catch (Exception e) {
            logger.warn("Failed to save api log", e);
        }
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
        if (auth == null) return null;
        return auth.getName();
    }

    private Map<String, String> extractHeaders(HttpServletRequest req) {
        return java.util.Collections.list(req.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(h -> h, req::getHeader));
    }
}
