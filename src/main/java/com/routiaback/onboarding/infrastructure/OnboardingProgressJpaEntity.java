package com.routiaback.onboarding.infrastructure;

import com.routiaback.onboarding.domain.OnboardingProgress;
import com.routiaback.onboarding.domain.OnboardingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "onboarding_progress")
class OnboardingProgressJpaEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OnboardingStatus status;

    @Column(name = "last_completed_step", nullable = false)
    private int lastCompletedStep;

    @Column(name = "step1_completed_at")
    private Instant step1CompletedAt;

    @Column(name = "step2_completed_at")
    private Instant step2CompletedAt;

    @Column(name = "step3_completed_at")
    private Instant step3CompletedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OnboardingProgressJpaEntity() {
    }

    private OnboardingProgressJpaEntity(OnboardingProgress progress) {
        this.userId = progress.userId();
        this.status = progress.status();
        this.lastCompletedStep = progress.lastCompletedStep();
        this.step1CompletedAt = progress.step1CompletedAt();
        this.step2CompletedAt = progress.step2CompletedAt();
        this.step3CompletedAt = progress.step3CompletedAt();
        this.completedAt = progress.completedAt();
        this.updatedAt = progress.updatedAt();
    }

    static OnboardingProgressJpaEntity from(OnboardingProgress progress) {
        return new OnboardingProgressJpaEntity(progress);
    }

    OnboardingProgress toDomain() {
        return new OnboardingProgress(userId, status, lastCompletedStep, step1CompletedAt,
                step2CompletedAt, step3CompletedAt, completedAt, updatedAt);
    }
}
