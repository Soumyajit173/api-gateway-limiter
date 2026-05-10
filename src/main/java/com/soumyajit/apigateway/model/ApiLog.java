package com.soumyajit.apigateway.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "api_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiLog {
    @Id
    private String id;

    private String method;
    private String path;
    private String clientIp;
    private String username;
    private int status;
    private long durationMs;
    private Instant timestamp;
    private Map<String, String> headers;
    private String requestBody;
    private String responseBody;
}
