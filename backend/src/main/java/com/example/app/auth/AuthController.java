package com.example.app.auth;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.app.auth.model.CurrentUserResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
	public CurrentUserResponse currentUser(Authentication authentication) {
		List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
		return new CurrentUserResponse(authentication.getName(), roles);
	}
}
