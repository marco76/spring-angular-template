package com.example.app.admin;

import java.time.Instant;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.app.admin.model.AdminOverviewResponse;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public AdminOverviewResponse overview() {
		return new AdminOverviewResponse("ok", "Admin area is reachable.", Instant.now());
	}
}
