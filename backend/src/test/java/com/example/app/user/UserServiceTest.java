package com.example.app.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.example.app.user.db.UserEntity;
import com.example.app.user.db.UserRepository;
import com.example.app.user.model.CreateUserRequest;
import com.example.app.user.model.UpdateUserRequest;
import com.example.app.user.types.UserRole;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	private UserService userService;

	@BeforeEach
	void setUp() {
		userService = new UserService(userRepository, passwordEncoder);
	}

	@Test
	void createUserRejectsDuplicateUsername() {
		given(userRepository.existsByUsernameIgnoreCase("admin")).willReturn(true);

		assertThatThrownBy(
				() -> userService.createUser(new CreateUserRequest("admin", "a-strong-password", UserRole.USER)))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("already taken");

		verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void deleteUserRejectsSelfDeletion() {
		UserEntity entity = new UserEntity("admin", "hash", UserRole.ADMIN, true);
		given(userRepository.findById(1L)).willReturn(Optional.of(entity));

		assertThatThrownBy(() -> userService.deleteUser(1L, "admin")).isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("own account");

		verify(userRepository, never()).delete(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void deleteUserRejectsRemovingLastEnabledAdmin() {
		UserEntity entity = new UserEntity("admin", "hash", UserRole.ADMIN, true);
		given(userRepository.findById(1L)).willReturn(Optional.of(entity));
		given(userRepository.countByRoleAndEnabledTrue(UserRole.ADMIN)).willReturn(1L);

		assertThatThrownBy(() -> userService.deleteUser(1L, "someone-else"))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("last remaining admin");
	}

	@Test
	void deleteUserAllowsRemovingNonLastAdmin() {
		UserEntity entity = new UserEntity("admin", "hash", UserRole.ADMIN, true);
		given(userRepository.findById(1L)).willReturn(Optional.of(entity));
		given(userRepository.countByRoleAndEnabledTrue(UserRole.ADMIN)).willReturn(2L);

		userService.deleteUser(1L, "someone-else");

		verify(userRepository).delete(entity);
	}

	@Test
	void updateUserAppliesPasswordOnlyWhenProvided() {
		UserEntity entity = new UserEntity("user1", "old-hash", UserRole.USER, true);
		given(userRepository.findById(1L)).willReturn(Optional.of(entity));

		userService.updateUser(1L, new UpdateUserRequest(UserRole.USER, true, null));

		assertThat(entity.getPasswordHash()).isEqualTo("old-hash");
		verify(passwordEncoder, never()).encode(anyString());
	}
}
