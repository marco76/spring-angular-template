package com.example.app.user.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.app.user.types.UserRole;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

	Optional<UserEntity> findByUsernameIgnoreCase(String username);

	boolean existsByUsernameIgnoreCase(String username);

	long countByRoleAndEnabledTrue(UserRole role);
}
