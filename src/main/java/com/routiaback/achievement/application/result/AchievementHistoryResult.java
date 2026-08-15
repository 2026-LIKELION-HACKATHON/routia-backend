package com.routiaback.achievement.application.result;

import java.time.LocalDate;
import java.util.List;

public record AchievementHistoryResult(List<WeekRecord> weeks) {
    public record WeekRecord(LocalDate weekStart, LocalDate weekEnd, int performanceRate, int completedCount, int totalCount) {}
}