package com.routiaback.personalization.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

interface PersonalizationProfileJpaRepository extends JpaRepository<PersonalizationProfileJpaEntity, Long> {
}
