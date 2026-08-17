package com.routiaback.routine.application.result;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record RoutineTodayResult(
        Long routineId,
        LocalDate date,
        String directionText,
        String homeComment,
        int completedCount,
        int totalCount,
        List<Item> items
) {
    public record Item(
            Long itemId,
            String timeSlot,
            String category,
            String title,
            String detail,
            String effectCode,
            String expectedEffect,
            int sortOrder,
            boolean completed,
            Instant completedAt
    ) {}
}
