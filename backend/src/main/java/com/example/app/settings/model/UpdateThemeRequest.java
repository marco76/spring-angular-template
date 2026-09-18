package com.example.app.settings.model;

import com.example.app.settings.types.AppTheme;

import jakarta.validation.constraints.NotNull;

public record UpdateThemeRequest(@NotNull AppTheme theme) {
}
