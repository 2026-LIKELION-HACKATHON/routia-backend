package com.routiaback.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class RoutineScheduleTest {

    private static final Instant NOW = Instant.parse("2026-08-15T00:00:00Z");

    @Test
    void createsActiveScheduleWithSeoulTimezoneAndNotificationsEnabled() {
        RoutineSchedule schedule = RoutineSchedule.create(
                1L, LocalTime.MIDNIGHT, Instant.parse("2026-08-15T15:00:00Z"), NOW);

        assertThat(schedule.notificationTime()).isEqualTo(LocalTime.MIDNIGHT);
        assertThat(schedule.timezone()).isEqualTo("Asia/Seoul");
        assertThat(schedule.active()).isTrue();
        assertThat(schedule.notificationEnabled()).isTrue();
    }

    @Test
    void updatesScheduleWithoutChangingCreatedTime() {
        RoutineSchedule schedule = RoutineSchedule.create(
                1L, LocalTime.MIDNIGHT, Instant.parse("2026-08-15T15:00:00Z"), NOW);

        RoutineSchedule updated = schedule.update(
                LocalTime.of(23, 59), Instant.parse("2026-08-15T14:59:00Z"), NOW.plusSeconds(60));

        assertThat(updated.notificationTime()).isEqualTo(LocalTime.of(23, 59));
        assertThat(updated.nextGenerationAt()).isEqualTo(Instant.parse("2026-08-15T14:59:00Z"));
        assertThat(updated.createdAt()).isEqualTo(NOW);
        assertThat(updated.updatedAt()).isEqualTo(NOW.plusSeconds(60));
    }

    @Test
    void createsGenerationScheduleWithoutNotificationAndPreservesTimeWhenDisabled() {
        RoutineSchedule schedule = RoutineSchedule.createWithoutNotification(
                1L, Instant.parse("2026-08-15T21:00:00Z"), NOW);
        assertThat(schedule.notificationEnabled()).isFalse();
        assertThat(schedule.notificationTime()).isNull();

        RoutineSchedule enabled = schedule.updateNotification(true, LocalTime.of(8, 0),
                Instant.parse("2026-08-15T22:50:00Z"), NOW.plusSeconds(1));
        RoutineSchedule disabled = enabled.updateNotification(false, null,
                Instant.parse("2026-08-15T22:50:00Z"), NOW.plusSeconds(2));
        assertThat(disabled.notificationEnabled()).isFalse();
        assertThat(disabled.notificationTime()).isEqualTo(LocalTime.of(8, 0));
    }
}
