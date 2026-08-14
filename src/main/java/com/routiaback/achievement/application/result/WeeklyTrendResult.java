package com.routiaback.achievement.application.result;

import java.time.LocalDate;
import java.util.List;

public record WeeklyTrendResult(List<DayTrend> days) {
    public record DayTrend(LocalDate date, int completedCount, int totalCount) {}
}