package com.example.app.error;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import com.example.app.error.model.ErrorResponse;

/**
 * Turns exceptions thrown from controller/service code into the app's
 * single JSON error shape. Controllers and services should let exceptions
 * propagate here instead of catching and formatting errors themselves.
 *
 * This only sees exceptions Spring MVC's dispatch can catch: business logic,
 * {@code ResponseStatusException}, validation, and method-level
 * {@code @PreAuthorize} checks. Exceptions thrown by the security filter
 * chain itself (URL-level {@code authorizeHttpRequests} rules, authentication
 * failures) never reach here — see the access-denied handler and
 * authentication entry point wired in {@code SecurityConfig}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException exception, WebRequest request) {
		HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
		String message = exception.getReason() != null ? exception.getReason() : status.getReasonPhrase();
		return ResponseEntity.status(status).body(ErrorResponses.of(status, message, pathOf(request)));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception, WebRequest request) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(ErrorResponses.of(HttpStatus.FORBIDDEN, "Access is denied.", pathOf(request)));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception,
			WebRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
			String reason = fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "is invalid";
			fieldErrors.put(fieldError.getField(), reason);
		}
		ErrorResponse base = ErrorResponses.of(HttpStatus.BAD_REQUEST, "Validation failed.", pathOf(request));
		ErrorResponse body = new ErrorResponse(base.status(), base.error(), base.message(), base.path(),
				base.timestamp(), fieldErrors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, WebRequest request) {
		log.error("Unhandled exception on {}", pathOf(request), exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponses
				.of(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", pathOf(request)));
	}

	private String pathOf(WebRequest request) {
		return request.getDescription(false).replace("uri=", "");
	}
}
