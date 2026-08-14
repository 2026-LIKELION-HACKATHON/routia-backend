package com.routiaback.onboarding.infrastructure;

import com.routiaback.onboarding.application.port.OnboardingProgressRepositoryPort;
import com.routiaback.onboarding.domain.OnboardingProgress;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class OnboardingPersistenceAdapter implements OnboardingProgressRepositoryPort {

    private final OnboardingProgressJpaRepository repository;

    public OnboardingPersistenceAdapter(OnboardingProgressJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<OnboardingProgress> findByUserId(Long userId) {
        return repository.findById(userId).map(OnboardingProgressJpaEntity::toDomain);
    }

    @Override
    public OnboardingProgress save(OnboardingProgress progress) {
        return repository.save(OnboardingProgressJpaEntity.from(progress)).toDomain();
    }
}
