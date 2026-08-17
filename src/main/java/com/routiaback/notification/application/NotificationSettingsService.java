package com.routiaback.notification.application;

import com.routiaback.auth.application.port.UserRepositoryPort;
import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.notification.application.port.RoutineScheduleRepositoryPort;
import com.routiaback.notification.domain.RoutineGenerationScheduleCalculator;
import com.routiaback.notification.domain.RoutineSchedule;
import com.routiaback.routine.application.port.DailyRoutineRepositoryPort;
import com.routiaback.routine.domain.RoutineStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationSettingsService {
    private final UserRepositoryPort users;
    private final RoutineScheduleRepositoryPort schedules;
    private final DailyRoutineRepositoryPort routines;
    private final RoutineGenerationScheduleCalculator calculator;
    private final Clock clock;

    public NotificationSettingsService(UserRepositoryPort users, RoutineScheduleRepositoryPort schedules,
            DailyRoutineRepositoryPort routines, RoutineGenerationScheduleCalculator calculator, Clock clock) {
        this.users = users;
        this.schedules = schedules;
        this.routines = routines;
        this.calculator = calculator;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public SettingsResult get(Long authenticatedUserId, Long userId) {
        validateUser(authenticatedUserId, userId);
        return schedules.findByUserId(userId)
                .map(schedule -> new SettingsResult(schedule.notificationEnabled(), schedule.notificationTime()))
                .orElse(new SettingsResult(false, null));
    }

    @Transactional
    public SettingsResult update(Long authenticatedUserId, Long userId, boolean enabled, LocalTime requestedTime) {
        validateUser(authenticatedUserId, userId);
        Instant now = clock.instant();
        RoutineSchedule current = schedules.findByUserId(userId).orElse(null);
        LocalTime effectiveTime = requestedTime != null ? requestedTime
                : current == null ? null : current.notificationTime();
        if (enabled && effectiveTime == null) throw new ApiException(ErrorCode.INVALID_NOTIFICATION_SETTINGS);
        String timezone = current == null ? calculator.defaultTimezone() : current.timezone();
        RoutineGenerationScheduleCalculator.ScheduledTarget target = enabled
                ? calculator.next(effectiveTime, timezone, now)
                : calculator.nextDefault(timezone, now);
        LocalDate today = now.atZone(ZoneId.of(timezone)).toLocalDate();
        boolean todayAlreadyReady = routines.findByUserIdAndRoutineDate(userId, today)
                .filter(routine -> routine.status() == RoutineStatus.READY)
                .isPresent();
        if (todayAlreadyReady && target.routineDate().equals(today)) {
            LocalDate nextDate = today.plusDays(1);
            target = enabled
                    ? calculator.forRoutineDate(nextDate, effectiveTime, timezone)
                    : calculator.defaultForRoutineDate(nextDate, timezone);
        }
        Instant nextGenerationAt = target.generationAt();
        RoutineSchedule updated = current == null
                ? enabled
                    ? RoutineSchedule.create(userId, effectiveTime, timezone, nextGenerationAt, now)
                    : RoutineSchedule.createWithoutNotification(userId, timezone, nextGenerationAt, now)
                : current.updateNotification(enabled, requestedTime, nextGenerationAt, now);
        RoutineSchedule saved = schedules.save(updated);
        return new SettingsResult(saved.notificationEnabled(), saved.notificationTime());
    }

    private void validateUser(Long authenticatedUserId, Long userId) {
        if (!Objects.equals(authenticatedUserId, userId)) throw new ApiException(ErrorCode.USER_DATA_ACCESS_DENIED);
        users.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND)).validateLoginAllowed();
    }

    public record SettingsResult(boolean notificationEnabled, LocalTime notificationTime) { }
}
