package com.example.app.health;

import java.time.Instant;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.app.health.model.HealthResponse;

@RestController
@RequestMapping("/api/health")
public class HealthController {

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public HealthResponse health() {
		return new HealthResponse("ok", Instant.now());
	}
}
