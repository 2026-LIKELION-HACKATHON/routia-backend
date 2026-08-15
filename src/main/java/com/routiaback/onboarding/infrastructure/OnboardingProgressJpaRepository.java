package com.routiaback.onboarding.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

interface OnboardingProgressJpaRepository extends JpaRepository<OnboardingProgressJpaEntity, Long> {
}
