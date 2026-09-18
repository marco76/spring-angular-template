package com.example.app.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.app.user.model.CreateUserRequest;
import com.example.app.user.model.UpdateUserRequest;
import com.example.app.user.model.UserResponse;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<UserResponse> listUsers(Pageable pageable) {
		return userService.listUsers(pageable);
	}

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse createUser(@Valid @RequestBody CreateUserRequest request, HttpServletResponse response) {
		UserResponse created = userService.createUser(request);
		String location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(created.id()).toUriString();
		response.setHeader("Location", location);
		return created;
	}

	@PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public UserResponse updateUser(@PathVariable("id") Long id, @Valid @RequestBody UpdateUserRequest request) {
		return userService.updateUser(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteUser(@PathVariable("id") Long id, Authentication authentication) {
		userService.deleteUser(id, authentication.getName());
	}
}
