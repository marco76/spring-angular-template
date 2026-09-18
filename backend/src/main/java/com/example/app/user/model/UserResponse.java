package com.example.app.user.model;

import java.time.Instant;

import com.example.app.user.types.UserRole;

public record UserResponse(Long id, String username, UserRole role, boolean enabled, Instant createdAt) {
}
