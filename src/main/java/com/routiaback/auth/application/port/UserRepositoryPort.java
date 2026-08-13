package com.routiaback.auth.application.port;

import com.routiaback.auth.domain.User;
import java.util.Optional;

public interface UserRepositoryPort {

	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);

	Optional<User> findById(Long id);

	User save(User user);
}
