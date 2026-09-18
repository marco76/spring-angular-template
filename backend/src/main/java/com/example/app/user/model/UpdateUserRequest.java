package com.example.app.user.model;

import com.example.app.user.types.UserRole;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Partial update: {@code password} is optional and left as-is when null or
 * blank; {@code role} and {@code enabled} are always applied.
 */
public record UpdateUserRequest(@NotNull UserRole role, @NotNull Boolean enabled,
		@Size(min = 8, max = 100) String password) {
}
