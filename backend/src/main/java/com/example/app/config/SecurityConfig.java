package com.example.app.config;

import java.io.IOException;
import java.time.Instant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		AccessDeniedHandler accessDeniedHandler = (request, response, exception) -> writeError(response,
				HttpStatus.FORBIDDEN, "Access is denied.", request.getRequestURI());
		AuthenticationEntryPoint authenticationEntryPoint = (request, response, exception) -> writeError(response,
				HttpStatus.UNAUTHORIZED, "Authentication is required.", request.getRequestURI());

		return http.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/api/health")
						.permitAll()
						.requestMatchers(HttpMethod.GET, "/api/settings/theme").permitAll()
						// H2 console is only registered when spring.h2.console.enabled=true
						// (application-local.yml); this rule is a no-op everywhere else.
						.requestMatchers("/h2-console/**").permitAll()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				// H2 console renders inside a frame; same-origin framing only, not a global disable.
				.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
				// Exceptions thrown by the security filter chain itself (URL-level rules
				// above, authentication failures) never reach GlobalExceptionHandler
				// because they happen before DispatcherServlet runs. Handle them here
				// with the same error shape instead of Spring Security's default blank
				// response. Written by hand rather than through an injected ObjectMapper:
				// this app currently has both Jackson 3 (tools.jackson, what Spring Boot
				// 4's own JacksonAutoConfiguration wires) and classic Jackson 2
				// (com.fasterxml.jackson, pulled in transitively by other dependencies)
				// on the classpath, and the security layer should not have to guess
				// which one a shared bean resolves to.
				.exceptionHandling(exceptions -> exceptions.accessDeniedHandler(accessDeniedHandler)
						.authenticationEntryPoint(authenticationEntryPoint))
				.httpBasic(basic -> {
				}).build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	private static void writeError(HttpServletResponse response, HttpStatus status, String message, String path)
			throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		String json = "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\",\"timestamp\":\"%s\"}"
				.formatted(status.value(), jsonEscape(status.getReasonPhrase()), jsonEscape(message),
						jsonEscape(path), Instant.now());
		response.getWriter().write(json);
	}

	private static String jsonEscape(String value) {
		return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
