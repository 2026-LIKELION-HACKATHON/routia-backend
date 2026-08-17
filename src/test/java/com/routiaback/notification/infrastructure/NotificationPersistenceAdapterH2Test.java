package com.routiaback.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.routiaback.notification.application.port.NotificationLogRepositoryPort;
import com.routiaback.notification.application.port.PushDeviceRepositoryPort;
import com.routiaback.notification.domain.NotificationStatus;
import com.routiaback.notification.domain.NotificationType;
import com.routiaback.notification.domain.PushDevice;
import com.routiaback.notification.domain.PushPlatform;
import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import com.routiaback.routine.application.port.DailyRoutineRepositoryPort;
import com.routiaback.routine.domain.DailyRoutine;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:notification;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "routia.jwt.secret=test-secret-that-is-at-least-32-bytes-long"
})
@Transactional
class NotificationPersistenceAdapterH2Test {
    private static final Instant NOW = Instant.parse("2026-08-17T00:00:00Z");

    @Autowired PushDeviceRepositoryPort devices;
    @Autowired NotificationLogRepositoryPort logs;
    @Autowired DailyRoutineRepositoryPort routines;

    @Test
    void updatesExistingFidOwnershipWithoutCreatingAnotherDevice() {
        PushDevice first = devices.save(PushDevice.register(1L, "shared-fid", PushPlatform.WEB, NOW));

        PushDevice transferred = devices.save(first.claim(2L, PushPlatform.WEB, NOW.plusSeconds(1)));

        assertThat(transferred.id()).isEqualTo(first.id());
        assertThat(devices.findByInstallationId("shared-fid").orElseThrow().userId()).isEqualTo(2L);
    }

    @Test
    void reservesOnlyOneLogForSameRoutineDeviceAndType() {
        PushDevice device = devices.save(PushDevice.register(1L, "fid-1", PushPlatform.WEB, NOW));

        assertThat(logs.reserve(100L, device.id(), NotificationType.DAILY_ROUTINE_READY, NOW)).isTrue();
        assertThat(logs.reserve(100L, device.id(), NotificationType.DAILY_ROUTINE_READY, NOW)).isFalse();
        assertThat(logs.find(100L, device.id(), NotificationType.DAILY_ROUTINE_READY).orElseThrow().status())
                .isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    void findsOnlyReadyDueRoutineWithNotificationTimestamp() {
        DailyRoutine due = readyRoutine(1L, NOW.minusSeconds(1));
        DailyRoutine future = readyRoutine(2L, NOW.plusSeconds(60));
        DailyRoutine withoutNotification = readyRoutine(3L, null);
        DailyRoutine generating = DailyRoutine.generating(4L, LocalDate.of(2026, 8, 17),
                RoutineDifficulty.SIMPLE, RoutineTimePreference.ANY, NOW.minusSeconds(1), NOW);
        DailyRoutine savedDue = routines.save(due);
        routines.save(future);
        routines.save(withoutNotification);
        routines.save(generating);

        assertThat(routines.findReadyDueForNotification(NOW))
                .extracting(DailyRoutine::id)
                .containsExactly(savedDue.id());
    }

    private DailyRoutine readyRoutine(Long userId, Instant notificationAt) {
        return DailyRoutine.generating(userId, LocalDate.of(2026, 8, 17),
                        RoutineDifficulty.SIMPLE, RoutineTimePreference.ANY, notificationAt, NOW)
                .ready(null, "direction", "comment", RoutineDifficulty.SIMPLE,
                        RoutineTimePreference.ANY, "{}", "{}", "model", "v1", NOW);
    }
}
