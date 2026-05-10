package com.soumyajit.apigateway.exception;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class ApiErrorTest {

    @Test
    void testApiErrorData() {
        Instant now = Instant.now();
        ApiError error = new ApiError(now, 400, "Bad Request", "Message", "/path");

        assertEquals(now, error.getTimestamp());
        assertEquals(400, error.getStatus());
        assertEquals("Bad Request", error.getError());
        assertEquals("Message", error.getMessage());
        assertEquals("/path", error.getPath());
    }
}