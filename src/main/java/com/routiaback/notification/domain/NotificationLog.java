package com.routiaback.notification.domain;

import java.time.Instant;

public record NotificationLog(
        Long id,
        Long routineId,
        Long pushDeviceId,
        NotificationType type,
        NotificationStatus status,
        Instant sentAt,
        String errorCode,
        Instant createdAt,
        Instant updatedAt
) {
    public NotificationLog sent(Instant now) {
        return new NotificationLog(id, routineId, pushDeviceId, type, NotificationStatus.SENT,
                now, null, createdAt, now);
    }

    public NotificationLog failed(String errorCode, Instant now) {
        return new NotificationLog(id, routineId, pushDeviceId, type, NotificationStatus.FAILED,
                null, errorCode, createdAt, now);
    }
}
