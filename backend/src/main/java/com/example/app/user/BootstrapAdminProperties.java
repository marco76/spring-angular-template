package com.example.app.user;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.bootstrap-admin")
public record BootstrapAdminProperties(@DefaultValue("") String username, @DefaultValue("") String password) {

	public boolean hasCredentials() {
		return !username.isBlank() && !password.isBlank();
	}
}
