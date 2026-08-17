package com.routiaback.notification.application;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import com.routiaback.routine.application.port.DailyRoutineRepositoryPort;
import com.routiaback.routine.domain.DailyRoutine;
import com.routiaback.routine.domain.RoutineStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class NotificationSchedulerTest {
    @Test
    void isolatesRoutineFailureAndContinuesOtherDueRoutine() {
        Instant now = Instant.parse("2026-08-17T00:00:00Z");
        DailyRoutineRepositoryPort routines = Mockito.mock(DailyRoutineRepositoryPort.class);
        NotificationDeliveryService delivery = Mockito.mock(NotificationDeliveryService.class);
        DailyRoutine first = routine(100L, 1L, now);
        DailyRoutine second = routine(200L, 2L, now);
        given(routines.findReadyDueForNotification(now)).willReturn(List.of(first, second));
        Mockito.doThrow(new IllegalStateException("provider failure")).when(delivery).deliver(first);

        new NotificationScheduler(routines, delivery, Clock.fixed(now, ZoneOffset.UTC)).sendDue();

        then(delivery).should().deliver(first);
        then(delivery).should().deliver(second);
    }

    private DailyRoutine routine(Long id, Long userId, Instant now) {
        return new DailyRoutine(id, userId, null, LocalDate.of(2026, 8, 17), RoutineStatus.READY,
                "direction", "comment", RoutineDifficulty.SIMPLE, RoutineTimePreference.ANY,
                "{}", "{}", null, "model", "v1", now, now, now, now);
    }
}
