package com.soumyajit.apigateway.controller;

import com.soumyajit.apigateway.model.ApiLog;
import com.soumyajit.apigateway.repository.ApiLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ApiLogRepository apiLogRepository;

    public AdminController(ApiLogRepository apiLogRepository) {
        this.apiLogRepository = apiLogRepository;
    }

    @GetMapping("/logs")
    public ResponseEntity<?> listLogs(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        Page<ApiLog> p = apiLogRepository.findAll(PageRequest.of(Math.max(0, page), Math.max(1, size)));
        return ResponseEntity.ok(p);
    }

    @GetMapping("/logs/{id}")
    public ResponseEntity<?> getLog(@PathVariable String id) {
        Optional<ApiLog> opt = apiLogRepository.findById(id);
        return opt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/logs/{id}")
    public ResponseEntity<?> deleteLog(@PathVariable String id) {
        if (!apiLogRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        apiLogRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
