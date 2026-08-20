package com.routiaback.home.application.result;

import java.time.LocalDate;
import java.util.List;

public record TodayDirectionResult(
        Long routineId,
        LocalDate date,
        String emoji,
        String title,
        String description,
        List<Section> sections
) {
    public record Section(
            String period,
            String label,
            String icon,
            List<Item> items
    ) {}

    public record Item(
            Long itemId,
            String timeSlot,
            String title,
            String detail
    ) {}
}
