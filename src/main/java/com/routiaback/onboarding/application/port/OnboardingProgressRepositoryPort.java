package com.routiaback.onboarding.application.port;

import com.routiaback.onboarding.domain.OnboardingProgress;
import java.util.Optional;

public interface OnboardingProgressRepositoryPort {

    Optional<OnboardingProgress> findByUserId(Long userId);

    OnboardingProgress save(OnboardingProgress progress);
}
