package com.routiaback.weather.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserProfileJpaRepository extends JpaRepository<UserProfileJpaEntity, Long> {
}