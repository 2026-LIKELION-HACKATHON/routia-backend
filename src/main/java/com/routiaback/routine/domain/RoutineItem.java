package com.routiaback.routine.domain;

import java.time.Instant;

public record RoutineItem(Long id, Long routineId, String timeSlot, String category, String title,
        String detail, String effectCode, String expectedEffect, int sortOrder, boolean completed,
        Instant completedAt, Instant createdAt, Instant updatedAt) {
    public static RoutineItem generated(Long routineId, RoutineTimeSlot timeSlot, RoutineCategory category,
            String title, String detail, String effectCode, String expectedEffect, int sortOrder, Instant now) {
        return new RoutineItem(null, routineId, timeSlot.name(), category.name(), title, detail,
                effectCode, expectedEffect, sortOrder, false, null, now, now);
    }
    public RoutineItem toggleCompleted(Instant now) {
        boolean value = !completed;
        return new RoutineItem(id, routineId, timeSlot, category, title, detail, effectCode,
                expectedEffect, sortOrder, value, value ? now : null, createdAt, now);
    }
}
