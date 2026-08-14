package com.routiaback.achievement.presentation;

import com.routiaback.achievement.application.result.AchievementSummaryResult;

public record AchievementSummaryResponse(
        int weeklyPerformanceRate,
        int previousWeekDiff,
        double avgCompletedCount,
        int streakDays
) {
    public static AchievementSummaryResponse from(AchievementSummaryResult result) {
        return new AchievementSummaryResponse(
                result.weeklyPerformanceRate(), result.previousWeekDiff(),
                result.avgCompletedCount(), result.streakDays()
        );
    }
}