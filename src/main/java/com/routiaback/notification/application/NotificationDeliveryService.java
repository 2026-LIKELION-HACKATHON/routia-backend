package com.routiaback.notification.application;

import com.routiaback.notification.application.port.PushDeviceRepositoryPort;
import com.routiaback.notification.application.port.PushNotificationPort;
import com.routiaback.notification.application.port.RoutineScheduleRepositoryPort;
import com.routiaback.notification.domain.NotificationType;
import com.routiaback.notification.domain.PushDevice;
import com.routiaback.notification.domain.RoutineReadyNotificationTemplate;
import com.routiaback.routine.domain.DailyRoutine;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationDeliveryService {
    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryService.class);
    private static final NotificationType TYPE = NotificationType.DAILY_ROUTINE_READY;

    private final RoutineScheduleRepositoryPort schedules;
    private final PushDeviceRepositoryPort devices;
    private final PushNotificationPort sender;
    private final NotificationDeliveryTransactionService transactions;
    private final Clock clock;

    public NotificationDeliveryService(RoutineScheduleRepositoryPort schedules,
            PushDeviceRepositoryPort devices, PushNotificationPort sender,
            NotificationDeliveryTransactionService transactions, Clock clock) {
        this.schedules = schedules;
        this.devices = devices;
        this.sender = sender;
        this.transactions = transactions;
        this.clock = clock;
    }

    public void deliver(DailyRoutine routine) {
        if (!notificationEnabled(routine.userId())) {
            return;
        }
        for (PushDevice device : devices.findAllActiveByUserId(routine.userId())) {
            try {
                deliverToDevice(routine, device);
            } catch (RuntimeException exception) {
                log.warn("Push device processing failed. routineId={} deviceId={} error={}",
                        routine.id(), device.id(), exception.getClass().getSimpleName());
            }
        }
    }

    private void deliverToDevice(DailyRoutine routine, PushDevice device) {
        if (!notificationEnabled(routine.userId())) {
            return;
        }
        Instant now = clock.instant();
        if (!transactions.reserve(routine.id(), device.id(), TYPE, now)) {
            return;
        }
        PushNotificationPort.DeliveryResult result;
        try {
            result = sender.send(device.token(), RoutineReadyNotificationTemplate.TITLE,
                    RoutineReadyNotificationTemplate.BODY,
                    RoutineReadyNotificationTemplate.data(routine.id(), routine.routineDate()));
        } catch (RuntimeException exception) {
            result = PushNotificationPort.DeliveryResult.failed("UNEXPECTED_PROVIDER_ERROR", false);
        }
        Instant completedAt = clock.instant();
        if (result.success()) {
            transactions.markSent(routine.id(), device.id(), TYPE, completedAt);
            return;
        }
        transactions.markFailed(routine.id(), device.id(), TYPE, normalize(result.errorCode()),
                result.permanentFailure(), device, completedAt);
        log.warn("Push delivery failed. routineId={} deviceId={} errorCode={}",
                routine.id(), device.id(), normalize(result.errorCode()));
    }

    private boolean notificationEnabled(Long userId) {
        return schedules.findByUserId(userId)
                .map(schedule -> schedule.active() && schedule.notificationEnabled())
                .orElse(false);
    }

    private String normalize(String errorCode) {
        if (errorCode == null || errorCode.isBlank()) {
            return "UNKNOWN";
        }
        return errorCode.length() > 100 ? errorCode.substring(0, 100) : errorCode;
    }
}
