package com.example.app.user.model;

import com.example.app.user.types.UserRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(@NotBlank @Size(max = 100) String username,
		@NotBlank @Size(min = 8, max = 100) String password, @NotNull UserRole role) {
}
