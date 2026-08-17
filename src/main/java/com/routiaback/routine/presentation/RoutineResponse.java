package com.routiaback.routine.presentation;

import com.routiaback.routine.application.result.RoutineTodayResult;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record RoutineResponse(
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

    public static RoutineResponse from(RoutineTodayResult result) {
        List<Item> items = result.items().stream()
                .map(i -> new Item(
                        i.itemId(), i.timeSlot(), i.category(), i.title(), i.detail(),
                        i.effectCode(), i.expectedEffect(), i.sortOrder(), i.completed(), i.completedAt()))
                .toList();
        return new RoutineResponse(
                result.routineId(), result.date(), result.directionText(), result.homeComment(),
                result.completedCount(), result.totalCount(), items);
    }
}
