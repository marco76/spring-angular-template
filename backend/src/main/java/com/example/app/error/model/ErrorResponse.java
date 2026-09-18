package com.example.app.error.model;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(int status, String error, String message, String path, Instant timestamp,
		Map<String, String> fieldErrors) {

	public ErrorResponse(int status, String error, String message, String path, Instant timestamp) {
		this(status, error, message, path, timestamp, null);
	}
}
