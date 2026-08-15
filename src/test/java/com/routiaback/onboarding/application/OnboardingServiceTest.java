package com.routiaback.onboarding.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.notification.application.port.RoutineScheduleRepositoryPort;
import com.routiaback.notification.domain.RoutineSchedule;
import com.routiaback.onboarding.application.command.Step1Command;
import com.routiaback.onboarding.application.command.Step2Command;
import com.routiaback.onboarding.application.command.Step3Command;
import com.routiaback.onboarding.application.port.OnboardingProgressRepositoryPort;
import com.routiaback.onboarding.domain.OnboardingProgress;
import com.routiaback.onboarding.domain.OnboardingStatus;
import com.routiaback.personalization.application.PersonalizationService;
import com.routiaback.personalization.application.command.UpdateNeedsCommand;
import com.routiaback.personalization.application.command.UpdateProfileCommand;
import com.routiaback.personalization.domain.AgeGroup;
import com.routiaback.personalization.domain.BodyGoal;
import com.routiaback.personalization.domain.Gender;
import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import com.routiaback.personalization.domain.SkinType;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OnboardingServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-15T00:00:00Z");
    private final PersonalizationService personalizationService = mock(PersonalizationService.class);
    private final FakeProgressRepository progressRepository = new FakeProgressRepository();
    private final FakeScheduleRepository scheduleRepository = new FakeScheduleRepository();
    private OnboardingService service;

    @BeforeEach
    void setUp() {
        service = new OnboardingService(personalizationService, progressRepository, scheduleRepository,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void step1UpdatesExistingUserDataAndProgress() {
        OnboardingProgress result = service.completeStep1(1L, new Step1Command(
                new BigDecimal("165.5"), new BigDecimal("55.2"), Gender.FEMALE, AgeGroup.TWENTIES,
                List.of("SWELLING", "SWELLING", "FATIGUE"), BodyGoal.MAINTAIN));

        ArgumentCaptor<UpdateProfileCommand> profile = ArgumentCaptor.forClass(UpdateProfileCommand.class);
        ArgumentCaptor<UpdateNeedsCommand> needs = ArgumentCaptor.forClass(UpdateNeedsCommand.class);
        verify(personalizationService).updateProfile(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(1L), profile.capture());
        verify(personalizationService).updateNeeds(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(1L), needs.capture());

        assertThat(profile.getValue().height()).isEqualByComparingTo("165.5");
        assertThat(needs.getValue().bodyGoal()).isEqualTo(BodyGoal.MAINTAIN);
        assertThat(needs.getValue().bodyConcerns()).containsExactly("SWELLING", "SWELLING", "FATIGUE");
        assertThat(result.status()).isEqualTo(OnboardingStatus.IN_PROGRESS);
        assertThat(result.lastCompletedStep()).isEqualTo(1);
    }

    @Test
    void resubmittingStep1AfterStep2UpdatesDataWithoutRegressingProgress() {
        progressRepository.progress = OnboardingProgress.notStarted(1L, NOW.minusSeconds(30))
                .completeStep1(NOW.minusSeconds(20))
                .completeStep2(NOW.minusSeconds(10));

        OnboardingProgress result = service.completeStep1(1L, step1Command());

        assertThat(result.lastCompletedStep()).isEqualTo(2);
    }

    @Test
    void step2RequiresStep1AndStoresFinalSkinSelections() {
        assertThatThrownBy(() -> service.completeStep2(
                1L, new Step2Command(SkinType.DRY, List.of("ACNE"))))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ONBOARDING_STEP_ORDER_INVALID);

        progressRepository.progress = OnboardingProgress.notStarted(1L, NOW).completeStep1(NOW);
        OnboardingProgress result = service.completeStep2(
                1L, new Step2Command(SkinType.DRY, List.of()));

        assertThat(result.lastCompletedStep()).isEqualTo(2);
        ArgumentCaptor<UpdateNeedsCommand> needs = ArgumentCaptor.forClass(UpdateNeedsCommand.class);
        verify(personalizationService).updateNeeds(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(1L), needs.capture());
        assertThat(needs.getValue().skinConcerns()).isEmpty();
    }

    @Test
    void step3RequiresStep2AndStoresPreferenceIndependentlyFromNotificationTime() {
        progressRepository.progress = OnboardingProgress.notStarted(1L, NOW).completeStep1(NOW);

        assertThatThrownBy(() -> service.completeStep3(1L, step3Command(LocalTime.of(10, 0))))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ONBOARDING_STEP_ORDER_INVALID);

        progressRepository.progress = progressRepository.progress.completeStep2(NOW);
        OnboardingProgress result = service.completeStep3(1L, step3Command(LocalTime.of(10, 0)));

        assertThat(result.lastCompletedStep()).isEqualTo(3);
        assertThat(scheduleRepository.schedule.notificationTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(scheduleRepository.schedule.nextGenerationAt())
                .isEqualTo(Instant.parse("2026-08-15T01:00:00Z"));
        ArgumentCaptor<UpdateNeedsCommand> needs = ArgumentCaptor.forClass(UpdateNeedsCommand.class);
        verify(personalizationService).updateNeeds(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(1L), needs.capture());
        assertThat(needs.getValue().routineTimePreference()).isEqualTo(RoutineTimePreference.MORNING);
    }

    @Test
    void calculatesNextScheduleOccurrenceInSeoulForPastTimeAndBoundaryTimes() {
        progressRepository.progress = completedStep2();
        service.completeStep3(1L, step3Command(LocalTime.of(8, 0)));
        assertThat(scheduleRepository.schedule.nextGenerationAt())
                .isEqualTo(Instant.parse("2026-08-15T23:00:00Z"));

        progressRepository.progress = completedStep2();
        service.completeStep3(1L, step3Command(LocalTime.MIDNIGHT));
        assertThat(scheduleRepository.schedule.nextGenerationAt())
                .isEqualTo(Instant.parse("2026-08-15T15:00:00Z"));

        progressRepository.progress = completedStep2();
        service.completeStep3(1L, step3Command(LocalTime.of(23, 59)));
        assertThat(scheduleRepository.schedule.nextGenerationAt())
                .isEqualTo(Instant.parse("2026-08-15T14:59:00Z"));
    }

    @Test
    void returnsInitialProgressWhenUserHasNeverStarted() {
        OnboardingProgress result = service.getProgress(1L);

        assertThat(result.status()).isEqualTo(OnboardingStatus.NOT_STARTED);
        assertThat(result.lastCompletedStep()).isZero();
    }

    @Test
    void doesNotMarkStepCompleteWhenUserDataOrScheduleSaveFails() {
        doThrow(new IllegalStateException("needs unavailable"))
                .when(personalizationService).updateNeeds(any(), any(), any());

        assertThatThrownBy(() -> service.completeStep1(1L, step1Command()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(progressRepository.progress).isNull();

        org.mockito.Mockito.reset(personalizationService);
        progressRepository.progress = completedStep2();
        scheduleRepository.failSave = true;

        assertThatThrownBy(() -> service.completeStep3(1L, step3Command(LocalTime.NOON)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(progressRepository.progress.lastCompletedStep()).isEqualTo(2);
    }

    private OnboardingProgress completedStep2() {
        return OnboardingProgress.notStarted(1L, NOW).completeStep1(NOW).completeStep2(NOW);
    }

    private Step1Command step1Command() {
        return new Step1Command(new BigDecimal("165.5"), new BigDecimal("55.2"), Gender.FEMALE,
                AgeGroup.TWENTIES, List.of("SWELLING"), BodyGoal.MAINTAIN);
    }

    private Step3Command step3Command(LocalTime notificationTime) {
        return new Step3Command(RoutineTimePreference.MORNING, RoutineDifficulty.SIMPLE, notificationTime);
    }

    private static class FakeProgressRepository implements OnboardingProgressRepositoryPort {
        private OnboardingProgress progress;

        @Override
        public Optional<OnboardingProgress> findByUserId(Long userId) {
            return Optional.ofNullable(progress);
        }

        @Override
        public OnboardingProgress save(OnboardingProgress progress) {
            this.progress = progress;
            return progress;
        }
    }

    private static class FakeScheduleRepository implements RoutineScheduleRepositoryPort {
        private RoutineSchedule schedule;
        private boolean failSave;

        @Override
        public Optional<RoutineSchedule> findByUserId(Long userId) {
            return Optional.ofNullable(schedule);
        }

        @Override
        public RoutineSchedule save(RoutineSchedule schedule) {
            if (failSave) {
                throw new IllegalStateException("schedule unavailable");
            }
            this.schedule = schedule;
            return schedule;
        }
    }
}
