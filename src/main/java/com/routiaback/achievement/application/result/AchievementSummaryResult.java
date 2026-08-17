package com.routiaback.achievement.application.result;

public record AchievementSummaryResult(
        int weeklyPerformanceRate,
        int previousWeekDiff,
        double avgCompletedCount,
        int streakDays
) {}