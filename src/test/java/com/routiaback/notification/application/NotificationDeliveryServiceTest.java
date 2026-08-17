package com.routiaback.notification.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.routiaback.notification.application.port.PushDeviceRepositoryPort;
import com.routiaback.notification.application.port.PushDeliveryErrorCode;
import com.routiaback.notification.application.port.PushNotificationPort;
import com.routiaback.notification.application.port.RoutineScheduleRepositoryPort;
import com.routiaback.notification.domain.NotificationType;
import com.routiaback.notification.domain.PushDevice;
import com.routiaback.notification.domain.PushPlatform;
import com.routiaback.notification.domain.RoutineReadyNotificationTemplate;
import com.routiaback.notification.domain.RoutineSchedule;
import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import com.routiaback.routine.domain.DailyRoutine;
import com.routiaback.routine.domain.RoutineStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class NotificationDeliveryServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-17T00:00:00Z");
    private final RoutineScheduleRepositoryPort schedules = Mockito.mock(RoutineScheduleRepositoryPort.class);
    private final PushDeviceRepositoryPort devices = Mockito.mock(PushDeviceRepositoryPort.class);
    private final PushNotificationPort sender = Mockito.mock(PushNotificationPort.class);
    private final NotificationDeliveryTransactionService transactions =
            Mockito.mock(NotificationDeliveryTransactionService.class);
    private final NotificationDeliveryService service = new NotificationDeliveryService(
            schedules, devices, sender, transactions, Clock.fixed(NOW, ZoneOffset.UTC));

    @BeforeEach
    void enabled() {
        given(schedules.findByUserId(1L)).willReturn(Optional.of(
                RoutineSchedule.create(1L, LocalTime.of(9, 0), NOW, NOW)));
    }

    @Test
    void sendsExpectedPayloadAndMarksLogSent() {
        PushDevice device = device(10L, "fid-1");
        given(devices.findAllActiveByUserId(1L)).willReturn(List.of(device));
        given(devices.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(device));
        given(transactions.reserve(100L, 10L, NotificationType.DAILY_ROUTINE_READY, NOW)).willReturn(true);
        given(sender.send(eq("fid-1"), any(), any(), anyMap()))
                .willReturn(PushNotificationPort.DeliveryResult.sent());

        service.deliver(routine());

        then(sender).should().send("fid-1", RoutineReadyNotificationTemplate.TITLE,
                RoutineReadyNotificationTemplate.BODY,
                RoutineReadyNotificationTemplate.data(100L, LocalDate.of(2026, 8, 17)));
        then(transactions).should().markSent(100L, 10L,
                NotificationType.DAILY_ROUTINE_READY, NOW);
    }

    @Test
    void skipsWhenSettingIsOffOrDeliveryWasAlreadyReserved() {
        given(schedules.findByUserId(1L)).willReturn(Optional.of(
                RoutineSchedule.createWithoutNotification(1L, NOW, NOW)));
        service.deliver(routine());
        then(sender).should(never()).send(any(), any(), any(), anyMap());

        Mockito.reset(schedules);
        given(schedules.findByUserId(1L)).willReturn(Optional.of(
                RoutineSchedule.create(1L, LocalTime.of(9, 0), NOW, NOW)));
        given(devices.findAllActiveByUserId(1L)).willReturn(List.of(device(10L, "fid-1")));
        given(devices.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(device(10L, "fid-1")));
        given(transactions.reserve(any(), any(), any(), any())).willReturn(false);
        service.deliver(routine());
        then(sender).should(never()).send(any(), any(), any(), anyMap());
    }

    @Test
    void keepsReadyRoutineWhenUserHasNoActiveDevice() {
        given(devices.findAllActiveByUserId(1L)).willReturn(List.of());

        service.deliver(routine());

        then(transactions).should(never()).reserve(any(), any(), any(), any());
        then(sender).should(never()).send(any(), any(), any(), anyMap());
    }

    @Test
    void permanentProviderFailureMarksFailedAndDeactivatesOnlyThatDevice() {
        PushDevice first = device(10L, "invalid-fid");
        PushDevice second = device(11L, "valid-fid");
        given(devices.findAllActiveByUserId(1L)).willReturn(List.of(first, second));
        given(devices.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(first));
        given(devices.findByIdAndUserId(11L, 1L)).willReturn(Optional.of(second));
        given(transactions.reserve(eq(100L), any(), any(), eq(NOW))).willReturn(true);
        given(sender.send(eq("invalid-fid"), any(), any(), anyMap()))
                .willReturn(PushNotificationPort.DeliveryResult.failed(
                        PushDeliveryErrorCode.UNREGISTERED, true));
        given(sender.send(eq("valid-fid"), any(), any(), anyMap()))
                .willReturn(PushNotificationPort.DeliveryResult.sent());

        service.deliver(routine());

        then(transactions).should().markFailed(100L, 10L, NotificationType.DAILY_ROUTINE_READY,
                "UNREGISTERED", true, first, NOW);
        then(transactions).should().markSent(100L, 11L,
                NotificationType.DAILY_ROUTINE_READY, NOW);
        then(sender).should(times(2)).send(any(), any(), any(), anyMap());
    }

    @Test
    void transactionFailureForOneDeviceDoesNotBlockAnotherDevice() {
        PushDevice first = device(10L, "first-fid");
        PushDevice second = device(11L, "second-fid");
        given(devices.findAllActiveByUserId(1L)).willReturn(List.of(first, second));
        given(devices.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(first));
        given(devices.findByIdAndUserId(11L, 1L)).willReturn(Optional.of(second));
        given(transactions.reserve(100L, 10L, NotificationType.DAILY_ROUTINE_READY, NOW))
                .willThrow(new IllegalStateException("database error"));
        given(transactions.reserve(100L, 11L, NotificationType.DAILY_ROUTINE_READY, NOW)).willReturn(true);
        given(sender.send(eq("second-fid"), any(), any(), anyMap()))
                .willReturn(PushNotificationPort.DeliveryResult.sent());

        service.deliver(routine());

        then(transactions).should().markSent(100L, 11L,
                NotificationType.DAILY_ROUTINE_READY, NOW);
    }

    @Test
    void skipsStaleDeviceSnapshotAfterTokenOwnershipChanged() {
        PushDevice stale = device(10L, "transferred-fid");
        given(devices.findAllActiveByUserId(1L)).willReturn(List.of(stale));
        given(devices.findByIdAndUserId(10L, 1L)).willReturn(Optional.empty());

        service.deliver(routine());

        then(transactions).should(never()).reserve(any(), any(), any(), any());
        then(sender).should(never()).send(any(), any(), any(), anyMap());
    }

    private PushDevice device(Long id, String installationId) {
        return new PushDevice(id, 1L, installationId, PushPlatform.WEB, true, NOW, NOW, NOW);
    }

    private DailyRoutine routine() {
        return new DailyRoutine(100L, 1L, null, LocalDate.of(2026, 8, 17), RoutineStatus.READY,
                "direction", "comment", RoutineDifficulty.SIMPLE, RoutineTimePreference.ANY,
                "{}", "{}", null, "model", "v1", NOW, NOW, NOW, NOW);
    }
}
