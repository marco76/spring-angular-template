package com.example.app.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.app.user.db.UserEntity;
import com.example.app.user.db.UserRepository;
import com.example.app.user.model.CreateUserRequest;
import com.example.app.user.model.UpdateUserRequest;
import com.example.app.user.model.UserResponse;
import com.example.app.user.types.UserRole;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public Page<UserResponse> listUsers(Pageable pageable) {
		return userRepository.findAll(pageable).map(UserService::toResponse);
	}

	@Transactional
	public UserResponse createUser(CreateUserRequest request) {
		if (userRepository.existsByUsernameIgnoreCase(request.username())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
		}
		UserEntity entity = new UserEntity(request.username(), passwordEncoder.encode(request.password()),
				request.role(), true);
		return toResponse(userRepository.save(entity));
	}

	@Transactional
	public UserResponse updateUser(Long id, UpdateUserRequest request) {
		UserEntity entity = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

		boolean stopsBeingEnabledAdmin = request.role() != UserRole.ADMIN || !request.enabled();
		if (isEnabledAdmin(entity) && stopsBeingEnabledAdmin && isLastEnabledAdmin()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot remove the last remaining admin.");
		}

		entity.setRole(request.role());
		entity.setEnabled(request.enabled());
		if (request.password() != null && !request.password().isBlank()) {
			entity.setPasswordHash(passwordEncoder.encode(request.password()));
		}
		return toResponse(entity);
	}

	@Transactional
	public void deleteUser(Long id, String currentUsername) {
		UserEntity entity = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

		if (entity.getUsername().equalsIgnoreCase(currentUsername)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "You cannot delete your own account.");
		}
		if (isEnabledAdmin(entity) && isLastEnabledAdmin()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot remove the last remaining admin.");
		}
		userRepository.delete(entity);
	}

	private static boolean isEnabledAdmin(UserEntity entity) {
		return entity.getRole() == UserRole.ADMIN && entity.isEnabled();
	}

	private boolean isLastEnabledAdmin() {
		return userRepository.countByRoleAndEnabledTrue(UserRole.ADMIN) <= 1;
	}

	private static UserResponse toResponse(UserEntity entity) {
		return new UserResponse(entity.getId(), entity.getUsername(), entity.getRole(), entity.isEnabled(),
				entity.getCreatedAt());
	}
}
