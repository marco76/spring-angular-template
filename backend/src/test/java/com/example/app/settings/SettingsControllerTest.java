package com.example.app.settings;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.app.config.SecurityConfig;
import com.example.app.settings.model.ThemeResponse;
import com.example.app.settings.types.AppTheme;

@WebMvcTest(SettingsController.class)
@Import(SecurityConfig.class)
class SettingsControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SettingsService settingsService;

	@Test
	void getThemeIsPublic() throws Exception {
		given(settingsService.getTheme()).willReturn(new ThemeResponse(AppTheme.GL));

		mockMvc.perform(get("/api/settings/theme"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.theme", is("GL")));
	}

	@Test
	void updateThemeRequiresAuthentication() throws Exception {
		mockMvc.perform(put("/api/admin/settings/theme")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"theme":"FT"}
						"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void updateThemeRejectsUserRole() throws Exception {
		mockMvc.perform(put("/api/admin/settings/theme")
				.with(user("user").roles("USER"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"theme":"FT"}
						"""))
				.andExpect(status().isForbidden());
	}

	@Test
	void updateThemeAllowsAdminRole() throws Exception {
		given(settingsService.updateTheme(any())).willReturn(new ThemeResponse(AppTheme.FT));

		mockMvc.perform(put("/api/admin/settings/theme")
				.with(user("admin").roles("ADMIN"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"theme":"FT"}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.theme", is("FT")));
	}
}
