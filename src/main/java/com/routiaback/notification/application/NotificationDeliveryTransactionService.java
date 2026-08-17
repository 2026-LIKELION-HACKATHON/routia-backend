package com.routiaback.notification.application;

import com.routiaback.notification.application.port.NotificationLogRepositoryPort;
import com.routiaback.notification.application.port.PushDeviceRepositoryPort;
import com.routiaback.notification.domain.NotificationLog;
import com.routiaback.notification.domain.NotificationType;
import com.routiaback.notification.domain.PushDevice;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationDeliveryTransactionService {
    private final NotificationLogRepositoryPort logs;
    private final PushDeviceRepositoryPort devices;

    public NotificationDeliveryTransactionService(NotificationLogRepositoryPort logs,
            PushDeviceRepositoryPort devices) {
        this.logs = logs;
        this.devices = devices;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean reserve(Long routineId, Long deviceId, NotificationType type, Instant now) {
        return logs.reserve(routineId, deviceId, type, now);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSent(Long routineId, Long deviceId, NotificationType type, Instant now) {
        NotificationLog log = find(routineId, deviceId, type);
        logs.save(log.sent(now));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long routineId, Long deviceId, NotificationType type, String errorCode,
            boolean deactivateDevice, PushDevice device, Instant now) {
        NotificationLog log = find(routineId, deviceId, type);
        logs.save(log.failed(errorCode, now));
        if (deactivateDevice && device.active()) {
            devices.save(device.deactivate(now));
        }
    }

    private NotificationLog find(Long routineId, Long deviceId, NotificationType type) {
        return logs.find(routineId, deviceId, type)
                .orElseThrow(() -> new IllegalStateException("reserved notification log not found"));
    }
}
