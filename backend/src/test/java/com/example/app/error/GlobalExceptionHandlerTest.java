package com.example.app.error;

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

import com.example.app.admin.AdminController;
import com.example.app.config.SecurityConfig;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class GlobalExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void accessDeniedFromPreAuthorizeIsMappedToTheConsistentErrorShape() throws Exception {
		mockMvc.perform(get("/api/admin").with(user("user").roles("USER")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status", is(403)))
				.andExpect(jsonPath("$.error", is("Forbidden")))
				.andExpect(jsonPath("$.message", is("Access is denied.")));
	}

	@Test
	void adminRoleIsAllowedThrough() throws Exception {
		mockMvc.perform(get("/api/admin").with(user("admin").roles("ADMIN", "USER"))).andExpect(status().isOk());
	}

	@Test
	void unauthenticatedRequestIsMappedToTheConsistentErrorShape() throws Exception {
		mockMvc.perform(get("/api/admin"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.error", is("Unauthorized")))
				.andExpect(jsonPath("$.message", is("Authentication is required.")));
	}
}
