package com.soumyajit.apigateway.filter;

import com.soumyajit.apigateway.model.RateLimitCounter;
import com.soumyajit.apigateway.repository.RateLimitRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;


@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitRepository rateLimitRepository;
    private final long capacity;
    private final long refillPerMinute;

    public RateLimitFilter(RateLimitRepository rateLimitRepository,
                           @Value("${ratelimit.capacity:100}") long capacity,
                           @Value("${ratelimit.refillPerMinute:100}") long refillPerMinute) {
        this.rateLimitRepository = rateLimitRepository;
        this.capacity = capacity;
        this.refillPerMinute = refillPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String key = resolveKey(request);
        if (key == null) {
            filterChain.doFilter(request, response);
            return;
        }

        synchronized (key.intern()) {
            Optional<RateLimitCounter> opt = rateLimitRepository.findByKey(key);
            RateLimitCounter counter = opt.orElseGet(() -> RateLimitCounter.builder()
                    .key(key)
                    .tokens(capacity)
                    .capacity(capacity)
                    .lastRefill(Instant.now())
                    .build());

            refill(counter);
            if (counter.getTokens() <= 0) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Rate limit exceeded");
                return;
            }

            counter.setTokens(counter.getTokens() - 1);
            counter.setLastRefill(Instant.now());
            rateLimitRepository.save(counter);
        }

        filterChain.doFilter(request, response);
    }

    private void refill(RateLimitCounter counter) {
        Instant now = Instant.now();
        long minutes = Math.max(0, Duration.between(counter.getLastRefill(), now).toMinutes());
        if (minutes <= 0) return;
        long toAdd = minutes * refillPerMinute;
        long newTokens = Math.min(counter.getCapacity(), counter.getTokens() + toAdd);
        counter.setTokens(newTokens);
        counter.setLastRefill(now);
    }

    private String resolveKey(HttpServletRequest req) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            return "user:" + auth.getName();
        }
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return "ip:" + xf.split(",")[0].trim();
        }
        return "ip:" + req.getRemoteAddr();
    }
}
