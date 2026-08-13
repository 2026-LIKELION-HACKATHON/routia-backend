package com.routiaback.routine.application.result;

import java.time.LocalDate;
import java.util.List;

public record RoutineTodayResult(
        LocalDate date,
        String directionText,
        int completedCount,
        int totalCount,
        List<Item> items
) {
    public record Item(Long itemId, String title, boolean completed) {}
}