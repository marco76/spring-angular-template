package com.example.app.admin.model;

import java.time.Instant;

public record AdminOverviewResponse(String status, String message, Instant checkedAt) {
}
