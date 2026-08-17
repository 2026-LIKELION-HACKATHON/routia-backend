package com.routiaback.notification.domain;

import java.time.*;
import org.springframework.stereotype.Component;

@Component
public class RoutineGenerationScheduleCalculator {
    public static final int GENERATION_LEAD_MINUTES = 10;
    public static final LocalTime DEFAULT_GENERATION_TIME = LocalTime.of(6, 0);

    public ScheduledTarget next(LocalTime notificationTime, String timezone, Instant now) {
        ZoneId zone = ZoneId.of(timezone); ZonedDateTime current = now.atZone(zone);
        ZonedDateTime notification = LocalDateTime.of(current.toLocalDate(), notificationTime).atZone(zone);
        ZonedDateTime generation = notification.minusMinutes(GENERATION_LEAD_MINUTES);
        if (!generation.isAfter(current)) { notification = notification.plusDays(1); generation = notification.minusMinutes(GENERATION_LEAD_MINUTES); }
        return new ScheduledTarget(notification.toLocalDate(), generation.toInstant(), notification.toInstant());
    }

    public ScheduledTarget current(RoutineSchedule schedule) {
        ZoneId zone = ZoneId.of(schedule.timezone());
        if (schedule.notificationTime() == null) {
            return new ScheduledTarget(schedule.nextGenerationAt().atZone(zone).toLocalDate(),
                    schedule.nextGenerationAt(), null);
        }
        Instant notification = schedule.nextGenerationAt().plusSeconds(GENERATION_LEAD_MINUTES * 60L);
        return new ScheduledTarget(notification.atZone(zone).toLocalDate(), schedule.nextGenerationAt(),
                schedule.notificationEnabled() ? notification : null);
    }

    public ScheduledTarget nextDefault(String timezone, Instant now) {
        ZoneId zone = ZoneId.of(timezone);
        ZonedDateTime current = now.atZone(zone);
        ZonedDateTime generation = LocalDateTime.of(current.toLocalDate(), DEFAULT_GENERATION_TIME).atZone(zone);
        if (!generation.isAfter(current)) generation = generation.plusDays(1);
        return new ScheduledTarget(generation.toLocalDate(), generation.toInstant(), null);
    }

    public ScheduledTarget next(RoutineSchedule schedule, Instant now) {
        return schedule.notificationTime() == null
                ? nextDefault(schedule.timezone(), now)
                : next(schedule.notificationTime(), schedule.timezone(), now);
    }

    public record ScheduledTarget(LocalDate routineDate, Instant generationAt, Instant notificationAt) { }
}
