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
) {}