package com.example.app.auth;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.example.app.config.SecurityConfig;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void currentUserRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
	}

	@Test
	void currentUserReturnsUsernameAndRoles() throws Exception {
		mockMvc.perform(get("/api/auth/me").with(user("admin").roles("ADMIN", "USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is("admin")))
				.andExpect(jsonPath("$.roles", is(List.of("ROLE_ADMIN", "ROLE_USER"))));
	}
}
