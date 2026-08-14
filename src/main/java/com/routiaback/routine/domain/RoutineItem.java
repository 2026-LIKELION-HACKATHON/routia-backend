package com.routiaback.routine.domain;

import java.time.Instant;

public record RoutineItem(
        Long id,
        Long routineId,
        String timeSlot,
        String category,
        String title,
        String detail,
        int sortOrder,
        boolean completed,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public RoutineItem toggleCompleted(Instant now) {
        boolean newCompleted = !completed;
        return new RoutineItem(id, routineId, timeSlot, category, title, detail, sortOrder,
                newCompleted, newCompleted ? now : null, createdAt, now);
    }
}