package io.polaris.sebrae.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.polaris.sebrae.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
}
