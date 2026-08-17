package com.routiaback.onboarding.application;

import com.routiaback.notification.application.port.RoutineScheduleRepositoryPort;
import com.routiaback.notification.domain.RoutineSchedule;
import com.routiaback.notification.domain.RoutineGenerationScheduleCalculator;
import com.routiaback.onboarding.application.command.Step1Command;
import com.routiaback.onboarding.application.command.Step0Command;
import com.routiaback.onboarding.application.command.Step2Command;
import com.routiaback.onboarding.application.command.Step3Command;
import com.routiaback.onboarding.application.port.OnboardingProgressRepositoryPort;
import com.routiaback.onboarding.domain.OnboardingProgress;
import com.routiaback.personalization.application.PersonalizationService;
import com.routiaback.personalization.application.command.UpdateNeedsCommand;
import com.routiaback.personalization.application.command.UpdateProfileCommand;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.routiaback.routine.application.generation.RoutineGenerationService;
import com.routiaback.routine.application.generation.GeneratedRoutine;
import com.routiaback.routine.domain.RoutineGenerationType;
import com.routiaback.onboarding.domain.OnboardingStatus;

@Service
public class OnboardingService {

    private static final ZoneId SCHEDULE_ZONE = ZoneId.of(RoutineSchedule.DEFAULT_TIMEZONE);

    private final PersonalizationService personalizationService;
    private final OnboardingProgressRepositoryPort progressRepository;
    private final RoutineScheduleRepositoryPort scheduleRepository;
    private final Clock clock;
    private final RoutineGenerationService routineGenerationService;
    private final RoutineGenerationScheduleCalculator scheduleCalculator;

    public OnboardingService(
            PersonalizationService personalizationService,
            OnboardingProgressRepositoryPort progressRepository,
            RoutineScheduleRepositoryPort scheduleRepository,
            Clock clock,
            RoutineGenerationService routineGenerationService,
            RoutineGenerationScheduleCalculator scheduleCalculator
    ) {
        this.personalizationService = personalizationService;
        this.progressRepository = progressRepository;
        this.scheduleRepository = scheduleRepository;
        this.clock = clock;
        this.routineGenerationService = routineGenerationService;
        this.scheduleCalculator = scheduleCalculator;
    }

    @Transactional
    public OnboardingProgress completeStep0(Long userId, Step0Command command) {
        OnboardingProgress progress = currentProgress(userId);
        personalizationService.updateUserName(userId, userId, command.userName());
        if (command.profileImage() != null) {
            personalizationService.uploadProfileImage(userId, userId, command.profileImage());
        }
        return progressRepository.save(progress.completeStep0(clock.instant()));
    }

    @Transactional
    public OnboardingProgress completeStep1(Long userId, Step1Command command) {
        OnboardingProgress progress = currentProgress(userId);
        OnboardingProgress completed = progress.completeStep1(clock.instant());
        personalizationService.updateProfile(userId, userId, new UpdateProfileCommand(
                command.height(), command.weight(), command.gender(), command.ageGroup(),
                command.regionSido(), command.regionSigungu(), command.latitude(), command.longitude(),
                command.locationSource()));
        return progressRepository.save(completed);
    }

    @Transactional
    public OnboardingProgress completeStep2(Long userId, Step2Command command) {
        OnboardingProgress progress = currentProgress(userId);
        OnboardingProgress completed = progress.completeStep2(clock.instant());
        personalizationService.updateNeeds(userId, userId, new UpdateNeedsCommand(
                null, command.bodyConcerns(), command.skinType(), command.skinConcerns(), null, null,
                command.bodyGoals(), command.ownedTools()));
        return progressRepository.save(completed);
    }

    @Transactional
    public OnboardingProgress completeStep3(Long userId, Step3Command command) {
        OnboardingProgress progress = currentProgress(userId);
        OnboardingProgress completed = progress.completeStep3(clock.instant());
        personalizationService.updateNeeds(userId, userId, new UpdateNeedsCommand(
                null, null, null, null, command.routineTimePreference(), command.routineDifficulty()));
        return progressRepository.save(completed);
    }

    @Transactional(readOnly = true)
    public OnboardingProgress getProgress(Long userId) {
        return currentProgress(userId);
    }

    public CompleteResult complete(Long userId) {
        OnboardingProgress progress = currentProgress(userId);
        LocalDate today = clock.instant().atZone(SCHEDULE_ZONE).toLocalDate();
        if (progress.status() == OnboardingStatus.COMPLETED) {
            CompleteResult result = completedResult(progress, routineGenerationService.generate(
                    userId, today, RoutineGenerationType.INITIAL_ONBOARDING, null));
            ensureGenerationSchedule(userId);
            return result;
        }
        OnboardingProgress generating = progressRepository.save(progress.startGenerating(clock.instant()));
        try {
            RoutineGenerationService.GenerationOutcome outcome = routineGenerationService.generate(
                    userId, today, RoutineGenerationType.INITIAL_ONBOARDING, null);
            requireReadyRoutine(outcome);
            ensureGenerationSchedule(userId);
            OnboardingProgress completed = progressRepository.save(generating.complete(clock.instant()));
            return new CompleteResult(completed, outcome.routineId(), outcome.routine());
        } catch (RuntimeException exception) {
            progressRepository.save(generating.fail(clock.instant()));
            throw exception;
        }
    }

    private CompleteResult completedResult(OnboardingProgress progress,
            RoutineGenerationService.GenerationOutcome outcome) {
        requireReadyRoutine(outcome);
        return new CompleteResult(progress, outcome.routineId(), outcome.routine());
    }

    private void requireReadyRoutine(RoutineGenerationService.GenerationOutcome outcome) {
        if (outcome.status() != com.routiaback.routine.domain.RoutineStatus.READY
                || outcome.routine() == null) {
            throw new com.routiaback.global.error.ApiException(
                    com.routiaback.global.error.ErrorCode.ROUTINE_GENERATION_FAILED);
        }
    }

    private OnboardingProgress currentProgress(Long userId) {
        return progressRepository.findByUserId(userId)
                .orElseGet(() -> OnboardingProgress.notStarted(userId, clock.instant()));
    }

    private void ensureGenerationSchedule(Long userId) {
        if (scheduleRepository.findByUserId(userId).isPresent()) return;
        Instant now = clock.instant();
        Instant next = scheduleCalculator.nextDefault(RoutineSchedule.DEFAULT_TIMEZONE, now).generationAt();
        scheduleRepository.save(RoutineSchedule.createWithoutNotification(userId, next, now));
    }

    public record CompleteResult(OnboardingProgress progress, Long routineId, GeneratedRoutine routine) { }

}
