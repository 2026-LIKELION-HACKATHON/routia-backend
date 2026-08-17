package com.routiaback.notification.domain;

import java.time.Instant;
import java.util.Objects;

public record PushDevice(
        Long id,
        Long userId,
        String token,
        PushPlatform platform,
        boolean active,
        Instant lastSeenAt,
        Instant createdAt,
        Instant updatedAt
) {
    public PushDevice {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(platform, "platform must not be null");
        Objects.requireNonNull(lastSeenAt, "lastSeenAt must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    public static PushDevice register(Long userId, String token, PushPlatform platform, Instant now) {
        return new PushDevice(null, userId, token, platform, true, now, now, now);
    }

    public PushDevice claim(Long newUserId, PushPlatform newPlatform, Instant now) {
        return new PushDevice(id, newUserId, token, newPlatform, true, now, createdAt, now);
    }

    public PushDevice deactivate(Instant now) {
        return new PushDevice(id, userId, token, platform, false, lastSeenAt, createdAt, now);
    }
}
