package com.soumyajit.apigateway.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "rate_limits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateLimitCounter {
    @Id
    private String id;

    private String key;
    private long tokens;
    private long capacity;
    private Instant lastRefill;
}
