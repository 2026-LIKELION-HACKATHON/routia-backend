package com.routiaback.home.application.result;

import java.time.LocalDate;
import java.util.List;

public record HomeResult(
        String userName,
        LocalDate date,
        String directionText,
        String homeComment,
        int progressPercent,
        int completedCount,
        int totalCount,
        List<TaskPreview> todayTasks
) {
    public record TaskPreview(Long itemId, String title, boolean completed) {}
}