package com.routiaback.auth.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

	boolean existsByEmail(String email);

	Optional<UserJpaEntity> findByEmail(String email);
}
