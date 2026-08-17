package com.routiaback.personalization.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserPreferenceJpaRepository extends JpaRepository<UserPreferenceJpaEntity, Long> {
}
