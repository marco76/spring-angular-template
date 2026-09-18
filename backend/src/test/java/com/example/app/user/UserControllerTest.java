package com.example.app.user;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.example.app.config.SecurityConfig;
import com.example.app.user.model.UserResponse;
import com.example.app.user.types.UserRole;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Test
	void listUsersRequiresAdminRole() throws Exception {
		mockMvc.perform(get("/api/admin/users").with(user("user").roles("USER"))).andExpect(status().isForbidden());
	}

	@Test
	void createUserReturnsCreatedForAdmin() throws Exception {
		given(userService.createUser(any())).willReturn(
				new UserResponse(1L, "new.admin", UserRole.USER, true, Instant.parse("2026-01-01T00:00:00Z")));

		mockMvc.perform(post("/api/admin/users").with(user("admin").roles("ADMIN"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"new.admin","password":"a-strong-password","role":"USER"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username", is("new.admin")));
	}

	@Test
	void createUserRejectsDuplicateUsername() throws Exception {
		given(userService.createUser(any()))
				.willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken."));

		mockMvc.perform(post("/api/admin/users").with(user("admin").roles("ADMIN"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"admin","password":"a-strong-password","role":"USER"}
						"""))
				.andExpect(status().isConflict());
	}

	@Test
	void deleteUserRejectsSelfDeletion() throws Exception {
		willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "You cannot delete your own account."))
				.given(userService)
				.deleteUser(eq(1L), eq("admin"));

		mockMvc.perform(delete("/api/admin/users/1").with(user("admin").roles("ADMIN")).with(csrf()))
				.andExpect(status().isConflict());
	}

	@Test
	void updateUserReturnsNotFoundForMissingUser() throws Exception {
		given(userService.updateUser(eq(99L), any()))
				.willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

		mockMvc.perform(patch("/api/admin/users/99").with(user("admin").roles("ADMIN"))
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"role":"USER","enabled":true}
						"""))
				.andExpect(status().isNotFound());
	}
}
