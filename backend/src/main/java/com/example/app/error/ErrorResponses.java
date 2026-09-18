package com.example.app.error;

import java.time.Instant;

import org.springframework.http.HttpStatus;

import com.example.app.error.model.ErrorResponse;

/**
 * Shared factory for the app's single error response shape, used by both
 * {@link GlobalExceptionHandler} (exceptions thrown from controller/service
 * code, reachable by Spring MVC) and the security filter chain's
 * access-denied/authentication-entry-point handlers in {@code SecurityConfig}
 * (exceptions thrown by the filter chain itself, before DispatcherServlet
 * runs, which {@code @RestControllerAdvice} never sees).
 */
public final class ErrorResponses {

	private ErrorResponses() {
	}

	public static ErrorResponse of(HttpStatus status, String message, String path) {
		return new ErrorResponse(status.value(), status.getReasonPhrase(), message, path, Instant.now());
	}
}
