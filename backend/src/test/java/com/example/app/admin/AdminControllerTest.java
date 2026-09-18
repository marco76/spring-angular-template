package com.example.app.admin;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.example.app.config.SecurityConfig;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void adminOverviewRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/admin")).andExpect(status().isUnauthorized());
	}

	@Test
	void adminOverviewRejectsUserRole() throws Exception {
		mockMvc.perform(get("/api/admin").with(user("user").roles("USER"))).andExpect(status().isForbidden());
	}

	@Test
	void adminOverviewAllowsAdminRole() throws Exception {
		mockMvc.perform(get("/api/admin").with(user("admin").roles("ADMIN", "USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("ok")));
	}
}
