package com.routiaback.home.presentation;

import com.routiaback.home.application.result.HomeResult;

import java.time.LocalDate;
import java.util.List;

public record HomeResponse(
        String userName,
        LocalDate date,
        int progressPercent,
        int completedCount,
        int totalCount,
        List<TaskPreview> todayTasks
) {
    public record TaskPreview(Long itemId, String title, boolean completed) {}

    public static HomeResponse from(HomeResult result) {
        List<TaskPreview> tasks = result.todayTasks().stream()
                .map(t -> new TaskPreview(t.itemId(), t.title(), t.completed()))
                .toList();
        return new HomeResponse(
                result.userName(),
                result.date(),
                result.progressPercent(),
                result.completedCount(),
                result.totalCount(),
                tasks
        );
    }
}