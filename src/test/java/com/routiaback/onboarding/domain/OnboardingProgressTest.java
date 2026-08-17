package com.routiaback.onboarding.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class OnboardingProgressTest {

    private static final Instant STEP1_AT = Instant.parse("2026-08-15T00:00:00Z");
    private static final Instant STEP2_AT = Instant.parse("2026-08-15T01:00:00Z");
    private static final Instant STEP3_AT = Instant.parse("2026-08-15T02:00:00Z");

    @Test
    void startsFromNotStartedAndStepZero() {
        OnboardingProgress progress = OnboardingProgress.notStarted(1L, STEP1_AT);

        assertThat(progress.status()).isEqualTo(OnboardingStatus.NOT_STARTED);
        assertThat(progress.lastCompletedStep()).isZero();
        assertThat(progress.step1CompletedAt()).isNull();
    }

    @Test
    void completesStepsInOrder() {
        OnboardingProgress progress = OnboardingProgress.notStarted(1L, STEP1_AT)
                .completeStep0(STEP1_AT)
                .completeStep1(STEP1_AT)
                .completeStep2(STEP2_AT)
                .completeStep3(STEP3_AT);

        assertThat(progress.status()).isEqualTo(OnboardingStatus.IN_PROGRESS);
        assertThat(progress.lastCompletedStep()).isEqualTo(3);
        assertThat(progress.step1CompletedAt()).isEqualTo(STEP1_AT);
        assertThat(progress.step2CompletedAt()).isEqualTo(STEP2_AT);
        assertThat(progress.step3CompletedAt()).isEqualTo(STEP3_AT);
    }

    @Test
    void rejectsStep2BeforeStep1AndStep3BeforeStep2() {
        OnboardingProgress initial = OnboardingProgress.notStarted(1L, STEP1_AT);

        assertThatThrownBy(() -> initial.completeStep2(STEP2_AT))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ONBOARDING_STEP_ORDER_INVALID);

        assertThatThrownBy(() -> initial.completeStep1(STEP1_AT))
                .isInstanceOf(ApiException.class);

        OnboardingProgress step1 = initial.completeStep0(STEP1_AT).completeStep1(STEP1_AT);
        assertThatThrownBy(() -> step1.completeStep3(STEP3_AT))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ONBOARDING_STEP_ORDER_INVALID);
    }

    @Test
    void resubmittingEarlierStepDoesNotRegressProgressOrCompletionTime() {
        OnboardingProgress step2 = OnboardingProgress.notStarted(1L, STEP1_AT)
                .completeStep0(STEP1_AT)
                .completeStep1(STEP1_AT)
                .completeStep2(STEP2_AT);

        OnboardingProgress resubmitted = step2.completeStep1(STEP3_AT);

        assertThat(resubmitted.lastCompletedStep()).isEqualTo(2);
        assertThat(resubmitted.step1CompletedAt()).isEqualTo(STEP1_AT);
        assertThat(resubmitted.step2CompletedAt()).isEqualTo(STEP2_AT);
    }

    @Test
    void resubmittingDataAfterCompletionDoesNotRegressCompletedStatus() {
        OnboardingProgress completed = OnboardingProgress.notStarted(1L, STEP1_AT)
                .completeStep0(STEP1_AT)
                .completeStep1(STEP1_AT)
                .completeStep2(STEP2_AT)
                .completeStep3(STEP3_AT)
                .startGenerating(STEP3_AT.plusSeconds(60))
                .complete(STEP3_AT.plusSeconds(120));

        OnboardingProgress resubmitted = completed.completeStep1(STEP3_AT.plusSeconds(180));

        assertThat(resubmitted.status()).isEqualTo(OnboardingStatus.COMPLETED);
        assertThat(resubmitted.lastCompletedStep()).isEqualTo(3);
        assertThat(resubmitted.completedAt()).isEqualTo(STEP3_AT.plusSeconds(120));
    }

    @Test
    void transitionsThroughGeneratingCompletedAndFailedStates() {
        OnboardingProgress step3 = OnboardingProgress.notStarted(1L, STEP1_AT)
                .completeStep0(STEP1_AT)
                .completeStep1(STEP1_AT)
                .completeStep2(STEP2_AT)
                .completeStep3(STEP3_AT);

        OnboardingProgress generating = step3.startGenerating(STEP3_AT.plusSeconds(60));
        OnboardingProgress completed = generating.complete(STEP3_AT.plusSeconds(120));
        OnboardingProgress failed = generating.fail(STEP3_AT.plusSeconds(180));

        assertThat(generating.status()).isEqualTo(OnboardingStatus.GENERATING);
        assertThat(completed.status()).isEqualTo(OnboardingStatus.COMPLETED);
        assertThat(completed.completedAt()).isEqualTo(STEP3_AT.plusSeconds(120));
        assertThat(failed.status()).isEqualTo(OnboardingStatus.FAILED);
        assertThat(failed.completedAt()).isNull();
    }
}
