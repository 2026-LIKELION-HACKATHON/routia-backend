package com.routiaback.routine.presentation;

import com.routiaback.routine.application.result.RoutineTodayResult;

import java.time.LocalDate;
import java.util.List;

public record RoutineResponse(
        LocalDate date,
        int completedCount,
        int totalCount,
        List<Item> items
) {
    public record Item(Long itemId, String timeSlot, String title, boolean completed) {}

    public static RoutineResponse from(RoutineTodayResult result) {
        List<Item> items = result.items().stream()
                .map(i -> new Item(i.itemId(), i.timeSlot(), i.title(), i.completed()))
                .toList();
        return new RoutineResponse(result.date(), result.completedCount(), result.totalCount(), items);
    }
}