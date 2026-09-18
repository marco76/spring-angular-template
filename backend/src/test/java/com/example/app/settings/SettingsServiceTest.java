package com.example.app.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.app.settings.db.SettingEntity;
import com.example.app.settings.db.SettingRepository;
import com.example.app.settings.types.AppTheme;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {

	@Mock
	private SettingRepository settingRepository;

	private SettingsService settingsService;

	@BeforeEach
	void setUp() {
		settingsService = new SettingsService(settingRepository);
	}

	@Test
	void getThemeReturnsDefaultWhenSettingDoesNotExist() {
		given(settingRepository.findById("ui.theme")).willReturn(Optional.empty());

		assertThat(settingsService.getTheme().theme()).isEqualTo(AppTheme.DEFAULT);
	}

	@Test
	void getThemeReturnsStoredTheme() {
		given(settingRepository.findById("ui.theme")).willReturn(Optional.of(new SettingEntity("ui.theme", "GL")));

		assertThat(settingsService.getTheme().theme()).isEqualTo(AppTheme.GL);
	}

	@Test
	void getThemeFallsBackToDefaultForUnknownStoredValue() {
		given(settingRepository.findById("ui.theme")).willReturn(Optional.of(new SettingEntity("ui.theme", "UNKNOWN")));

		assertThat(settingsService.getTheme().theme()).isEqualTo(AppTheme.DEFAULT);
	}

	@Test
	void updateThemeCreatesSettingWhenMissing() {
		given(settingRepository.findById("ui.theme")).willReturn(Optional.empty());

		assertThat(settingsService.updateTheme(AppTheme.FT).theme()).isEqualTo(AppTheme.FT);

		ArgumentCaptor<SettingEntity> captor = ArgumentCaptor.forClass(SettingEntity.class);
		verify(settingRepository).save(captor.capture());
		assertThat(captor.getValue().getKey()).isEqualTo("ui.theme");
		assertThat(captor.getValue().getValue()).isEqualTo("FT");
	}

	@Test
	void updateThemeReusesExistingSetting() {
		SettingEntity entity = new SettingEntity("ui.theme", "DEFAULT");
		given(settingRepository.findById("ui.theme")).willReturn(Optional.of(entity));

		settingsService.updateTheme(AppTheme.GL);

		assertThat(entity.getValue()).isEqualTo("GL");
		verify(settingRepository).save(entity);
	}
}
