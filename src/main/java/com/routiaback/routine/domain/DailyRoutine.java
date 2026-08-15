package com.routiaback.routine.domain;

import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import java.time.Instant;
import java.time.LocalDate;

public record DailyRoutine(Long id, Long userId, Long weatherSnapshotId, LocalDate routineDate,
        RoutineStatus status, String directionText, String homeComment,
        RoutineDifficulty difficultySnapshot, RoutineTimePreference timePreferenceSnapshot,
        String personalizationSnapshot, String performanceSnapshot, String generationErrorCode,
        String aiModel, String promptVersion, Instant generatedAt, Instant notificationScheduledAt,
        Instant createdAt, Instant updatedAt) {

    public static DailyRoutine generating(Long userId, LocalDate date, RoutineDifficulty difficulty,
            RoutineTimePreference timePreference, Instant notificationAt, Instant now) {
        return new DailyRoutine(null, userId, null, date, RoutineStatus.GENERATING, null, null,
                difficulty, timePreference, null, null, null, null, null, null, notificationAt, now, now);
    }

    public DailyRoutine ready(Long weatherId, String direction, String comment,
            RoutineDifficulty difficulty, RoutineTimePreference timePreference,
            String personalization, String performance, String model, String version, Instant now) {
        return new DailyRoutine(id, userId, weatherId, routineDate, RoutineStatus.READY, direction, comment,
                difficulty, timePreference, personalization, performance, null, model, version, now,
                notificationScheduledAt, createdAt, now);
    }

    public DailyRoutine failed(String errorCode, Instant now) {
        return new DailyRoutine(id, userId, weatherSnapshotId, routineDate, RoutineStatus.FAILED,
                directionText, homeComment, difficultySnapshot, timePreferenceSnapshot,
                personalizationSnapshot, performanceSnapshot, errorCode, aiModel, promptVersion,
                generatedAt, notificationScheduledAt, createdAt, now);
    }
}
