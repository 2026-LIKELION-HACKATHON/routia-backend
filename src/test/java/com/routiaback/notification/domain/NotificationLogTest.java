package com.routiaback.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class NotificationLogTest {
    @Test
    void transitionsPendingToSentOrFailed() {
        Instant createdAt = Instant.parse("2026-08-17T00:00:00Z");
        Instant completedAt = createdAt.plusSeconds(1);
        NotificationLog pending = new NotificationLog(1L, 10L, 20L,
                NotificationType.DAILY_ROUTINE_READY, NotificationStatus.PENDING,
                null, null, createdAt, createdAt);

        assertThat(pending.sent(completedAt).status()).isEqualTo(NotificationStatus.SENT);
        assertThat(pending.sent(completedAt).sentAt()).isEqualTo(completedAt);
        assertThat(pending.failed("UNAVAILABLE", completedAt).status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(pending.failed("UNAVAILABLE", completedAt).errorCode()).isEqualTo("UNAVAILABLE");
    }
}
