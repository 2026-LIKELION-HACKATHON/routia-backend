package com.routiaback.onboarding.application;

import com.routiaback.notification.application.port.RoutineScheduleRepositoryPort;
import com.routiaback.notification.domain.RoutineSchedule;
import com.routiaback.notification.domain.RoutineGenerationScheduleCalculator;
import com.routiaback.onboarding.application.command.Step1Command;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.routiaback.routine.application.generation.RoutineGenerationService;
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
    public OnboardingProgress completeStep1(Long userId, Step1Command command) {
        OnboardingProgress progress = currentProgress(userId);
        personalizationService.updateProfile(userId, userId, new UpdateProfileCommand(
                command.height(), command.weight(), command.gender(), command.ageGroup(),
                null, null, null, null, null));
        personalizationService.updateNeeds(userId, userId, new UpdateNeedsCommand(
                command.bodyGoal(), command.bodyConcerns(), null, null, null, null));
        return progressRepository.save(progress.completeStep1(clock.instant()));
    }

    @Transactional
    public OnboardingProgress completeStep2(Long userId, Step2Command command) {
        OnboardingProgress progress = currentProgress(userId);
        OnboardingProgress completed = progress.completeStep2(clock.instant());
        personalizationService.updateNeeds(userId, userId, new UpdateNeedsCommand(
                null, null, command.skinType(), command.skinConcerns(), null, null));
        return progressRepository.save(completed);
    }

    @Transactional
    public OnboardingProgress completeStep3(Long userId, Step3Command command) {
        OnboardingProgress progress = currentProgress(userId);
        Instant now = clock.instant();
        OnboardingProgress completed = progress.completeStep3(now);
        personalizationService.updateNeeds(userId, userId, new UpdateNeedsCommand(
                null, null, null, null, command.routineTimePreference(), command.routineDifficulty()));

        Instant nextGenerationAt = scheduleCalculator.next(command.notificationTime(),
                RoutineSchedule.DEFAULT_TIMEZONE, now).generationAt();
        RoutineSchedule schedule = scheduleRepository.findByUserId(userId)
                .map(current -> current.update(command.notificationTime(), nextGenerationAt, now))
                .orElseGet(() -> RoutineSchedule.create(
                        userId, command.notificationTime(), nextGenerationAt, now));
        scheduleRepository.save(schedule);
        return progressRepository.save(completed);
    }

    @Transactional(readOnly = true)
    public OnboardingProgress getProgress(Long userId) {
        return currentProgress(userId);
    }

    public OnboardingProgress complete(Long userId) {
        OnboardingProgress progress = currentProgress(userId);
        if (progress.status() == OnboardingStatus.COMPLETED) return progress;
        OnboardingProgress generating = progressRepository.save(progress.startGenerating(clock.instant()));
        LocalDate today = clock.instant().atZone(SCHEDULE_ZONE).toLocalDate();
        try {
            RoutineGenerationService.GenerationOutcome outcome = routineGenerationService.generate(
                    userId, today, RoutineGenerationType.INITIAL_ONBOARDING, null);
            if (outcome.status() != com.routiaback.routine.domain.RoutineStatus.READY) {
                throw new com.routiaback.global.error.ApiException(
                        com.routiaback.global.error.ErrorCode.ROUTINE_GENERATION_FAILED);
            }
            return progressRepository.save(generating.complete(clock.instant()));
        } catch (RuntimeException exception) {
            progressRepository.save(generating.fail(clock.instant()));
            throw exception;
        }
    }

    private OnboardingProgress currentProgress(Long userId) {
        return progressRepository.findByUserId(userId)
                .orElseGet(() -> OnboardingProgress.notStarted(userId, clock.instant()));
    }

}
