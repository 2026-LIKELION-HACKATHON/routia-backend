package com.routiaback.achievement.presentation;

import com.routiaback.achievement.application.result.AchievementHistoryResult;

import java.time.LocalDate;
import java.util.List;

public record AchievementHistoryResponse(List<WeekRecord> weeks) {
    public record WeekRecord(LocalDate weekStart, LocalDate weekEnd, int performanceRate, int completedCount, int totalCount) {}

    public static AchievementHistoryResponse from(AchievementHistoryResult result) {
        List<WeekRecord> weeks = result.weeks().stream()
                .map(w -> new WeekRecord(w.weekStart(), w.weekEnd(), w.performanceRate(), w.completedCount(), w.totalCount()))
                .toList();
        return new AchievementHistoryResponse(weeks);
    }
}