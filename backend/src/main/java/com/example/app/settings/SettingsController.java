package com.example.app.settings;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.app.settings.model.ThemeResponse;
import com.example.app.settings.model.UpdateThemeRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class SettingsController {

	private final SettingsService settingsService;

	@GetMapping(value = "/api/settings/theme", produces = MediaType.APPLICATION_JSON_VALUE)
	public ThemeResponse getTheme() {
		return settingsService.getTheme();
	}

	@PutMapping(value = "/api/admin/settings/theme", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ThemeResponse updateTheme(@Valid @RequestBody UpdateThemeRequest request) {
		return settingsService.updateTheme(request.theme());
	}
}
