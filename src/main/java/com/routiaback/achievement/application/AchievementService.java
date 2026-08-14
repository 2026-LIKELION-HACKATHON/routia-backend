package com.routiaback.achievement.application;

import com.routiaback.achievement.application.result.AchievementSummaryResult;
import com.routiaback.achievement.application.result.WeeklyTrendResult;
import com.routiaback.routine.application.RoutineService;
import com.routiaback.routine.application.result.DailyStat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private static final double STREAK_THRESHOLD = 0.5;
    private static final int STREAK_LOOKUP_DAYS = 90;

    private final RoutineService routineService;

    public AchievementSummaryResult getSummary(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate thisWeekStart = today.with(DayOfWeek.MONDAY);
        LocalDate thisWeekEnd = thisWeekStart.plusDays(6);
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = thisWeekEnd.minusWeeks(1);
        LocalDate streakRangeStart = today.minusDays(STREAK_LOOKUP_DAYS);

        List<DailyStat> stats = routineService.getStats(userId, streakRangeStart, today);
        Map<LocalDate, DailyStat> statsByDate = stats.stream()
                .collect(Collectors.toMap(DailyStat::date, s -> s));

        List<DailyStat> thisWeekStats = filterRange(stats, thisWeekStart, thisWeekEnd);
        List<DailyStat> lastWeekStats = filterRange(stats, lastWeekStart, lastWeekEnd);

        int thisWeekRate = calcRatePercent(thisWeekStats);
        int lastWeekRate = calcRatePercent(lastWeekStats);
        int diff = thisWeekRate - lastWeekRate;

        double avgCompleted = thisWeekStats.isEmpty()
                ? 0
                : Math.round(thisWeekStats.stream().mapToInt(DailyStat::completedCount).average().orElse(0) * 10) / 10.0;

        int streakDays = calculateStreak(statsByDate, today);

        return new AchievementSummaryResult(thisWeekRate, diff, avgCompleted, streakDays);
    }

    private List<DailyStat> filterRange(List<DailyStat> stats, LocalDate start, LocalDate end) {
        return stats.stream()
                .filter(s -> !s.date().isBefore(start) && !s.date().isAfter(end))
                .toList();
    }

    private int calcRatePercent(List<DailyStat> stats) {
        int totalCompleted = stats.stream().mapToInt(DailyStat::completedCount).sum();
        int totalCount = stats.stream().mapToInt(DailyStat::totalCount).sum();
        return totalCount == 0 ? 0 : (int) Math.round(totalCompleted * 100.0 / totalCount);
    }

    private int calculateStreak(Map<LocalDate, DailyStat> statsByDate, LocalDate today) {
        int streak = 0;
        LocalDate date = today;
        while (true) {
            DailyStat stat = statsByDate.get(date);
            if (stat == null || stat.totalCount() == 0) break;
            double rate = (double) stat.completedCount() / stat.totalCount();
            if (rate < STREAK_THRESHOLD) break;
            streak++;
            date = date.minusDays(1);
        }
        return streak;
    }

    public WeeklyTrendResult getWeeklyTrend(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        List<DailyStat> stats = routineService.getStats(userId, weekStart, weekEnd);
        Map<LocalDate, DailyStat> statsByDate = stats.stream()
                .collect(Collectors.toMap(DailyStat::date, s -> s));

        List<WeeklyTrendResult.DayTrend> dayTrends = weekStart.datesUntil(weekEnd.plusDays(1))
                .map(date -> {
                    DailyStat stat = statsByDate.getOrDefault(date, new DailyStat(date, 0, 0));
                    return new WeeklyTrendResult.DayTrend(date, stat.completedCount(), stat.totalCount());
                })
                .toList();

        return new WeeklyTrendResult(dayTrends);
    }
}