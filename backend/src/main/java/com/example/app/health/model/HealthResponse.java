package com.example.app.health.model;

import java.time.Instant;

public record HealthResponse(String status, Instant timestamp) {
}
