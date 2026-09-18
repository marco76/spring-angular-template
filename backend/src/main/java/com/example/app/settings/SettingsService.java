package com.example.app.settings;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.settings.db.SettingEntity;
import com.example.app.settings.db.SettingRepository;
import com.example.app.settings.model.ThemeResponse;
import com.example.app.settings.types.AppTheme;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettingsService {

	private static final String THEME_SETTING_KEY = "ui.theme";
	private static final AppTheme DEFAULT_THEME = AppTheme.DEFAULT;

	private final SettingRepository settingRepository;

	@Transactional(readOnly = true)
	public ThemeResponse getTheme() {
		AppTheme theme = settingRepository.findById(THEME_SETTING_KEY)
				.map(SettingEntity::getValue)
				.map(SettingsService::parseTheme)
				.orElse(DEFAULT_THEME);
		return new ThemeResponse(theme);
	}

	@Transactional
	public ThemeResponse updateTheme(AppTheme theme) {
		SettingEntity setting = settingRepository.findById(THEME_SETTING_KEY)
				.orElseGet(() -> new SettingEntity(THEME_SETTING_KEY, theme.name()));
		setting.setValue(theme.name());
		settingRepository.save(setting);
		return new ThemeResponse(theme);
	}

	private static AppTheme parseTheme(String value) {
		try {
			return AppTheme.valueOf(value);
		} catch (IllegalArgumentException exception) {
			return DEFAULT_THEME;
		}
	}
}
