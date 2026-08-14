package com.routiaback.onboarding.domain;

import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import java.time.Instant;
import java.util.Objects;

public record OnboardingProgress(
        Long userId,
        OnboardingStatus status,
        int lastCompletedStep,
        Instant step1CompletedAt,
        Instant step2CompletedAt,
        Instant step3CompletedAt,
        Instant completedAt,
        Instant updatedAt
) {

    public OnboardingProgress {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        validateState(status, lastCompletedStep, step1CompletedAt, step2CompletedAt,
                step3CompletedAt, completedAt);
    }

    public static OnboardingProgress notStarted(Long userId, Instant now) {
        return new OnboardingProgress(userId, OnboardingStatus.NOT_STARTED, 0,
                null, null, null, null, now);
    }

    public OnboardingProgress completeStep1(Instant now) {
        return new OnboardingProgress(userId, statusAfterStepSubmission(),
                Math.max(lastCompletedStep, 1), firstCompletion(step1CompletedAt, now),
                step2CompletedAt, step3CompletedAt, completedAtAfterStepSubmission(), now);
    }

    public OnboardingProgress completeStep2(Instant now) {
        requireCompletedStep(1);
        return new OnboardingProgress(userId, statusAfterStepSubmission(),
                Math.max(lastCompletedStep, 2), step1CompletedAt,
                firstCompletion(step2CompletedAt, now), step3CompletedAt,
                completedAtAfterStepSubmission(), now);
    }

    public OnboardingProgress completeStep3(Instant now) {
        requireCompletedStep(2);
        return new OnboardingProgress(userId, statusAfterStepSubmission(),
                3, step1CompletedAt, step2CompletedAt,
                firstCompletion(step3CompletedAt, now), completedAtAfterStepSubmission(), now);
    }

    public OnboardingProgress startGenerating(Instant now) {
        requireCompletedStep(3);
        return new OnboardingProgress(userId, OnboardingStatus.GENERATING, 3,
                step1CompletedAt, step2CompletedAt, step3CompletedAt, null, now);
    }

    public OnboardingProgress complete(Instant now) {
        if (status != OnboardingStatus.GENERATING) {
            throw new ApiException(ErrorCode.ONBOARDING_STEP_ORDER_INVALID);
        }
        return new OnboardingProgress(userId, OnboardingStatus.COMPLETED, 3,
                step1CompletedAt, step2CompletedAt, step3CompletedAt, now, now);
    }

    public OnboardingProgress fail(Instant now) {
        if (status != OnboardingStatus.GENERATING) {
            throw new ApiException(ErrorCode.ONBOARDING_STEP_ORDER_INVALID);
        }
        return new OnboardingProgress(userId, OnboardingStatus.FAILED, 3,
                step1CompletedAt, step2CompletedAt, step3CompletedAt, null, now);
    }

    private void requireCompletedStep(int requiredStep) {
        if (lastCompletedStep < requiredStep) {
            throw new ApiException(ErrorCode.ONBOARDING_STEP_ORDER_INVALID);
        }
    }

    private OnboardingStatus statusAfterStepSubmission() {
        return status == OnboardingStatus.NOT_STARTED ? OnboardingStatus.IN_PROGRESS : status;
    }

    private Instant completedAtAfterStepSubmission() {
        return status == OnboardingStatus.COMPLETED ? completedAt : null;
    }

    private static Instant firstCompletion(Instant current, Instant now) {
        return current == null ? Objects.requireNonNull(now, "completion time must not be null") : current;
    }

    private static void validateState(
            OnboardingStatus status,
            int lastCompletedStep,
            Instant step1CompletedAt,
            Instant step2CompletedAt,
            Instant step3CompletedAt,
            Instant completedAt
    ) {
        if (lastCompletedStep < 0 || lastCompletedStep > 3
                || (lastCompletedStep >= 1) != (step1CompletedAt != null)
                || (lastCompletedStep >= 2) != (step2CompletedAt != null)
                || (lastCompletedStep >= 3) != (step3CompletedAt != null)
                || status == OnboardingStatus.NOT_STARTED && lastCompletedStep != 0
                || status == OnboardingStatus.IN_PROGRESS && lastCompletedStep == 0
                || (status == OnboardingStatus.GENERATING
                    || status == OnboardingStatus.COMPLETED
                    || status == OnboardingStatus.FAILED) && lastCompletedStep != 3
                || (status == OnboardingStatus.COMPLETED) != (completedAt != null)) {
            throw new IllegalArgumentException("inconsistent onboarding progress state");
        }
    }
}
