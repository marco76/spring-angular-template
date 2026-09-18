package com.example.app.user;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.user.db.UserEntity;
import com.example.app.user.db.UserRepository;
import com.example.app.user.types.UserRole;

import lombok.RequiredArgsConstructor;

/**
 * Adds the non-admin {@code user / user} demo account for local development,
 * alongside the admin account {@link UserBootstrapRunner} creates from
 * {@code app.bootstrap-admin.*} (also set to demo values in
 * {@code application-local.yml}). See docs/KNOWN_LIMITATIONS.md.
 */
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDemoUserSeeder implements ApplicationRunner {

	private static final String DEMO_USERNAME = "user";
	private static final String DEMO_PASSWORD = "user";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (userRepository.existsByUsernameIgnoreCase(DEMO_USERNAME)) {
			return;
		}
		userRepository.save(new UserEntity(DEMO_USERNAME, passwordEncoder.encode(DEMO_PASSWORD), UserRole.USER,
				true));
	}
}
