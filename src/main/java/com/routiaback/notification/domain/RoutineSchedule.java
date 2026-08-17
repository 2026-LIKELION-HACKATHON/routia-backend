package com.routiaback.notification.domain;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Objects;

public record RoutineSchedule(
        Long userId,
        LocalTime notificationTime,
        String timezone,
        boolean active,
        boolean notificationEnabled,
        Instant nextGenerationAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static final String DEFAULT_TIMEZONE = "Asia/Seoul";

    public RoutineSchedule {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(timezone, "timezone must not be null");
        Objects.requireNonNull(nextGenerationAt, "nextGenerationAt must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    public static RoutineSchedule create(
            Long userId,
            LocalTime notificationTime,
            Instant nextGenerationAt,
            Instant now
    ) {
        return new RoutineSchedule(userId, notificationTime, DEFAULT_TIMEZONE, true, true,
                nextGenerationAt, now, now);
    }

    public static RoutineSchedule createWithoutNotification(
            Long userId, Instant nextGenerationAt, Instant now
    ) {
        return new RoutineSchedule(userId, null, DEFAULT_TIMEZONE, true, false,
                nextGenerationAt, now, now);
    }

    public RoutineSchedule update(LocalTime notificationTime, Instant nextGenerationAt, Instant now) {
        return new RoutineSchedule(userId, notificationTime, timezone, active, notificationEnabled,
                nextGenerationAt, createdAt, now);
    }

    public RoutineSchedule updateNotification(
            boolean enabled, LocalTime requestedTime, Instant nextGenerationAt, Instant now
    ) {
        LocalTime resolvedTime = requestedTime == null ? notificationTime : requestedTime;
        if (enabled && resolvedTime == null) {
            throw new IllegalArgumentException("notificationTime is required when notification is enabled");
        }
        return new RoutineSchedule(userId, resolvedTime, timezone, active, enabled,
                nextGenerationAt, createdAt, now);
    }

    public RoutineSchedule advance(Instant nextGenerationAt, Instant now) {
        return new RoutineSchedule(userId, notificationTime, timezone, active, notificationEnabled,
                nextGenerationAt, createdAt, now);
    }
}
