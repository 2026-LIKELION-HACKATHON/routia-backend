package com.routiaback.notification.domain;

import java.time.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RoutineGenerationScheduleCalculator {
    public static final int GENERATION_LEAD_MINUTES = 10;
    public static final LocalTime DEFAULT_GENERATION_TIME = LocalTime.MIDNIGHT;
    private final int generationLeadMinutes;
    private final LocalTime defaultGenerationTime;
    private final String defaultTimezone;

    public RoutineGenerationScheduleCalculator() {
        this(GENERATION_LEAD_MINUTES, DEFAULT_GENERATION_TIME, RoutineSchedule.DEFAULT_TIMEZONE);
    }

    @Autowired
    public RoutineGenerationScheduleCalculator(
            @Value("${routia.routine-generation.lead-minutes:10}") int generationLeadMinutes,
            @Value("${routia.routine-generation.default-time:00:00}") LocalTime defaultGenerationTime,
            @Value("${routia.routine-generation.default-timezone:Asia/Seoul}") String defaultTimezone) {
        this.generationLeadMinutes = generationLeadMinutes;
        this.defaultGenerationTime = defaultGenerationTime;
        ZoneId.of(defaultTimezone);
        this.defaultTimezone = defaultTimezone;
    }

    public String defaultTimezone() {
        return defaultTimezone;
    }

    public ScheduledTarget next(LocalTime notificationTime, String timezone, Instant now) {
        ZoneId zone = ZoneId.of(timezone); ZonedDateTime current = now.atZone(zone);
        ZonedDateTime notification = LocalDateTime.of(current.toLocalDate(), notificationTime).atZone(zone);
        ZonedDateTime generation = notification.minusMinutes(generationLeadMinutes);
        if (!generation.isAfter(current)) { notification = notification.plusDays(1); generation = notification.minusMinutes(generationLeadMinutes); }
        return new ScheduledTarget(notification.toLocalDate(), generation.toInstant(), notification.toInstant());
    }

    public ScheduledTarget current(RoutineSchedule schedule) {
        ZoneId zone = ZoneId.of(schedule.timezone());
        if (!schedule.notificationEnabled() || schedule.notificationTime() == null) {
            return new ScheduledTarget(schedule.nextGenerationAt().atZone(zone).toLocalDate(),
                    schedule.nextGenerationAt(), null);
        }
        Instant notification = schedule.nextGenerationAt().plusSeconds(generationLeadMinutes * 60L);
        return new ScheduledTarget(notification.atZone(zone).toLocalDate(), schedule.nextGenerationAt(),
                schedule.notificationEnabled() ? notification : null);
    }

    public ScheduledTarget nextDefault(String timezone, Instant now) {
        ZoneId zone = ZoneId.of(timezone);
        ZonedDateTime current = now.atZone(zone);
        ZonedDateTime generation = LocalDateTime.of(current.toLocalDate(), defaultGenerationTime).atZone(zone);
        if (!generation.isAfter(current)) generation = generation.plusDays(1);
        return new ScheduledTarget(generation.toLocalDate(), generation.toInstant(), null);
    }

    public ScheduledTarget next(RoutineSchedule schedule, Instant now) {
        return !schedule.notificationEnabled() || schedule.notificationTime() == null
                ? nextDefault(schedule.timezone(), now)
                : next(schedule.notificationTime(), schedule.timezone(), now);
    }

    public ScheduledTarget forRoutineDate(LocalDate routineDate, LocalTime notificationTime, String timezone) {
        ZoneId zone = ZoneId.of(timezone);
        ZonedDateTime notification = LocalDateTime.of(routineDate, notificationTime).atZone(zone);
        return new ScheduledTarget(routineDate, notification.minusMinutes(generationLeadMinutes).toInstant(),
                notification.toInstant());
    }

    public ScheduledTarget defaultForRoutineDate(LocalDate routineDate, String timezone) {
        Instant generation = LocalDateTime.of(routineDate, defaultGenerationTime)
                .atZone(ZoneId.of(timezone)).toInstant();
        return new ScheduledTarget(routineDate, generation, null);
    }

    public record ScheduledTarget(LocalDate routineDate, Instant generationAt, Instant notificationAt) { }
}
