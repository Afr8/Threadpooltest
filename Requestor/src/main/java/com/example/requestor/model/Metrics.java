package com.example.requestor.model;

import java.time.OffsetDateTime;

public record Metrics(
        String type,
        OffsetDateTime timestamp,
        long successfulRequests,
        long failedRequests,
        long minDurationMs,
        long maxDurationMs,
        double avgDurationMs,
        long totalDurationAllRequestsMs) {
}
