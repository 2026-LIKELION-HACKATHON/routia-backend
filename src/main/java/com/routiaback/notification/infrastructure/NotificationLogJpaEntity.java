package com.routiaback.notification.infrastructure;

import com.routiaback.notification.domain.NotificationLog;
import com.routiaback.notification.domain.NotificationStatus;
import com.routiaback.notification.domain.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "notification_logs", uniqueConstraints = @UniqueConstraint(
        name = "uk_notification_logs_delivery",
        columnNames = {"routine_id", "push_device_id", "notification_type"}))
class NotificationLogJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "routine_id", nullable = false)
    private Long routineId;
    @Column(name = "push_device_id", nullable = false)
    private Long pushDeviceId;
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 40)
    private NotificationType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;
    @Column(name = "sent_at")
    private Instant sentAt;
    @Column(name = "error_code", length = 100)
    private String errorCode;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected NotificationLogJpaEntity() {
    }

    private NotificationLogJpaEntity(NotificationLog log) {
        id = log.id();
        routineId = log.routineId();
        pushDeviceId = log.pushDeviceId();
        type = log.type();
        status = log.status();
        sentAt = log.sentAt();
        errorCode = log.errorCode();
        createdAt = log.createdAt();
        updatedAt = log.updatedAt();
    }

    static NotificationLogJpaEntity from(NotificationLog log) {
        return new NotificationLogJpaEntity(log);
    }

    NotificationLog toDomain() {
        return new NotificationLog(id, routineId, pushDeviceId, type, status, sentAt,
                errorCode, createdAt, updatedAt);
    }
}
