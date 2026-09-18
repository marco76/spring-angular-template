package com.example.app.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.user.db.UserEntity;
import com.example.app.user.db.UserRepository;
import com.example.app.user.types.UserRole;

/**
 * Creates the first admin account when {@code app_user} is empty, from
 * {@code app.bootstrap-admin.username}/{@code app.bootstrap-admin.password}.
 * Local development sets these (see {@code application-local.yml}) to the
 * demo credentials documented in {@code docs/KNOWN_LIMITATIONS.md}; other
 * environments must set them explicitly (for example via
 * {@code APP_BOOTSTRAP-ADMIN_USERNAME}/{@code APP_BOOTSTRAP-ADMIN_PASSWORD})
 * or create the first admin manually. Once at least one user exists, the
 * admin API (see {@code UserController}) is the intended way to manage
 * accounts.
 */
@Component
public class UserBootstrapRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(UserBootstrapRunner.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final String bootstrapUsername;
	private final String bootstrapPassword;

	public UserBootstrapRunner(UserRepository userRepository, PasswordEncoder passwordEncoder,
			@Value("${app.bootstrap-admin.username:}") String bootstrapUsername,
			@Value("${app.bootstrap-admin.password:}") String bootstrapPassword) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.bootstrapUsername = bootstrapUsername;
		this.bootstrapPassword = bootstrapPassword;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (userRepository.count() > 0) {
			return;
		}
		if (bootstrapUsername.isBlank() || bootstrapPassword.isBlank()) {
			log.warn("No users exist and app.bootstrap-admin.username/password are not set; "
					+ "create the first admin account manually before signing in.");
			return;
		}
		userRepository.save(new UserEntity(bootstrapUsername, passwordEncoder.encode(bootstrapPassword),
				UserRole.ADMIN, true));
		log.info("Created initial admin account '{}'.", bootstrapUsername);
	}
}
