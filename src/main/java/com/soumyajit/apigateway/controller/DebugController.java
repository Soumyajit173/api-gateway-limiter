package com.soumyajit.apigateway.controller;

import com.soumyajit.apigateway.model.RateLimitCounter;
import com.soumyajit.apigateway.repository.RateLimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    private final RateLimitRepository rateLimitRepository;

    @GetMapping("/ratelimits")
    public ResponseEntity<?> listAllCounters() {
        List<RateLimitCounter> all = rateLimitRepository.findAll();
        return ResponseEntity.ok(all);
    }

    @DeleteMapping("/ratelimits/{id}")
    public ResponseEntity<?> deleteCounter(@PathVariable String id) {
        if (!rateLimitRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        rateLimitRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
