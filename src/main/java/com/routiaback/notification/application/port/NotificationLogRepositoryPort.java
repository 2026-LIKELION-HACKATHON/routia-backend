package com.routiaback.notification.application.port;

import com.routiaback.notification.domain.NotificationLog;
import com.routiaback.notification.domain.NotificationType;
import java.time.Instant;
import java.util.Optional;

public interface NotificationLogRepositoryPort {
    boolean reserve(Long routineId, Long pushDeviceId, NotificationType type, Instant now);
    Optional<NotificationLog> find(Long routineId, Long pushDeviceId, NotificationType type);
    NotificationLog save(NotificationLog log);
}
