package com.routiaback.achievement.presentation;

import com.routiaback.achievement.application.result.WeeklyTrendResult;

import java.time.LocalDate;
import java.util.List;

public record WeeklyTrendResponse(List<DayTrend> days) {
    public record DayTrend(LocalDate date, int completedCount, int totalCount) {}

    public static WeeklyTrendResponse from(WeeklyTrendResult result) {
        List<DayTrend> days = result.days().stream()
                .map(d -> new DayTrend(d.date(), d.completedCount(), d.totalCount()))
                .toList();
        return new WeeklyTrendResponse(days);
    }
}