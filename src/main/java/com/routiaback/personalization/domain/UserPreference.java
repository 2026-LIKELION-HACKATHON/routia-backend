package com.routiaback.personalization.domain;

import java.time.Instant;

public record UserPreference(
        Long userId,
        BodyGoal bodyGoal,
        SkinType skinType,
        RoutineTimePreference routineTimePreference,
        RoutineDifficulty routineDifficulty,
        Instant createdAt,
        Instant updatedAt
) {

    public static UserPreference empty(Long userId, Instant now) {
        return new UserPreference(userId, null, null, null, null, now, now);
    }

    public UserPreference update(
            BodyGoal newBodyGoal,
            SkinType newSkinType,
            RoutineTimePreference newRoutineTimePreference,
            RoutineDifficulty newRoutineDifficulty,
            Instant now
    ) {
        return new UserPreference(
                userId,
                valueOrCurrent(newBodyGoal, bodyGoal),
                valueOrCurrent(newSkinType, skinType),
                valueOrCurrent(newRoutineTimePreference, routineTimePreference),
                valueOrCurrent(newRoutineDifficulty, routineDifficulty),
                createdAt,
                now
        );
    }

    private static <T> T valueOrCurrent(T value, T current) {
        return value == null ? current : value;
    }
}
