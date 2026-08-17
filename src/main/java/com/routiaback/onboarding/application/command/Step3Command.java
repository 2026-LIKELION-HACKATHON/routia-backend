package com.routiaback.onboarding.application.command;

import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import java.time.LocalTime;

public record Step3Command(
        RoutineTimePreference routineTimePreference,
        RoutineDifficulty routineDifficulty
) {
    public Step3Command(RoutineTimePreference routineTimePreference,
            RoutineDifficulty routineDifficulty, LocalTime ignoredNotificationTime) {
        this(routineTimePreference, routineDifficulty);
    }
}
