package com.routiaback.notification.infrastructure;

import com.routiaback.notification.domain.PushDevice;
import com.routiaback.notification.domain.PushPlatform;
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
@Table(name = "push_devices", uniqueConstraints =
        @UniqueConstraint(name = "uk_push_devices_token", columnNames = "token"))
class PushDeviceJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(nullable = false, length = 512)
    private String token;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PushPlatform platform;
    @Column(name = "is_active", nullable = false)
    private boolean active;
    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PushDeviceJpaEntity() {
    }

    private PushDeviceJpaEntity(PushDevice device) {
        id = device.id();
        userId = device.userId();
        token = device.token();
        platform = device.platform();
        active = device.active();
        lastSeenAt = device.lastSeenAt();
        createdAt = device.createdAt();
        updatedAt = device.updatedAt();
    }

    static PushDeviceJpaEntity from(PushDevice device) {
        return new PushDeviceJpaEntity(device);
    }

    PushDevice toDomain() {
        return new PushDevice(id, userId, token, platform, active, lastSeenAt, createdAt, updatedAt);
    }
}
