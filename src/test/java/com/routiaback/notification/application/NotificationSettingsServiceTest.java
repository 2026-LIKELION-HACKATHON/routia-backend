package com.routiaback.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.routiaback.auth.application.port.UserRepositoryPort;
import com.routiaback.auth.domain.User;
import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.notification.application.port.RoutineScheduleRepositoryPort;
import com.routiaback.notification.domain.RoutineGenerationScheduleCalculator;
import com.routiaback.notification.domain.RoutineSchedule;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationSettingsServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-15T00:00:00Z");
    private final FakeUserRepository users = new FakeUserRepository();
    private final FakeScheduleRepository schedules = new FakeScheduleRepository();
    private NotificationSettingsService service;

    @BeforeEach
    void setUp() {
        users.user = User.create("user@example.com", "hash", "Soeun", NOW).withId(1L);
        service = new NotificationSettingsService(users, schedules,
                new RoutineGenerationScheduleCalculator(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void returnsDisabledWhenUserHasNoSchedule() {
        NotificationSettingsService.SettingsResult result = service.get(1L, 1L);
        assertThat(result.notificationEnabled()).isFalse();
        assertThat(result.notificationTime()).isNull();
    }

    @Test
    void enablesNotificationAndCalculatesNextGenerationTenMinutesEarlier() {
        NotificationSettingsService.SettingsResult result = service.update(1L, 1L, true, LocalTime.of(10, 0));
        assertThat(result.notificationEnabled()).isTrue();
        assertThat(result.notificationTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(schedules.schedule.nextGenerationAt()).isEqualTo(Instant.parse("2026-08-15T00:50:00Z"));
    }

    @Test
    void disablesNotificationWithoutDeletingPreviousTime() {
        service.update(1L, 1L, true, LocalTime.of(10, 0));
        NotificationSettingsService.SettingsResult result = service.update(1L, 1L, false, null);
        assertThat(result.notificationEnabled()).isFalse();
        assertThat(result.notificationTime()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void rejectsEnableWithoutAnyNotificationTimeAndOtherUserAccess() {
        assertThatThrownBy(() -> service.update(1L, 1L, true, null))
                .isInstanceOf(ApiException.class).extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_NOTIFICATION_SETTINGS);
        assertThatThrownBy(() -> service.get(1L, 2L))
                .isInstanceOf(ApiException.class).extracting("errorCode")
                .isEqualTo(ErrorCode.USER_DATA_ACCESS_DENIED);
    }

    private static class FakeUserRepository implements UserRepositoryPort {
        private User user;
        @Override public boolean existsByEmail(String email) { return false; }
        @Override public Optional<User> findByEmail(String email) { return Optional.empty(); }
        @Override public Optional<User> findById(Long id) { return user != null && user.id().equals(id) ? Optional.of(user) : Optional.empty(); }
        @Override public User save(User user) { this.user = user; return user; }
    }

    private static class FakeScheduleRepository implements RoutineScheduleRepositoryPort {
        private RoutineSchedule schedule;
        @Override public Optional<RoutineSchedule> findByUserId(Long userId) { return Optional.ofNullable(schedule); }
        @Override public RoutineSchedule save(RoutineSchedule schedule) { this.schedule = schedule; return schedule; }
        @Override public List<RoutineSchedule> findDueActive(Instant now) { return List.of(); }
    }
}
