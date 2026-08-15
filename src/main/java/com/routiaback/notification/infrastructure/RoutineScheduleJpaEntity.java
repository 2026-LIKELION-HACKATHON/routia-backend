package com.routiaback.notification.infrastructure;

import com.routiaback.notification.domain.RoutineSchedule;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(name = "routine_schedules")
class RoutineScheduleJpaEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "notification_time", nullable = false)
    private LocalTime notificationTime;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "notification_enabled", nullable = false)
    private boolean notificationEnabled;

    @Column(name = "next_generation_at", nullable = false)
    private Instant nextGenerationAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RoutineScheduleJpaEntity() {
    }

    private RoutineScheduleJpaEntity(RoutineSchedule schedule) {
        this.userId = schedule.userId();
        this.notificationTime = schedule.notificationTime();
        this.timezone = schedule.timezone();
        this.active = schedule.active();
        this.notificationEnabled = schedule.notificationEnabled();
        this.nextGenerationAt = schedule.nextGenerationAt();
        this.createdAt = schedule.createdAt();
        this.updatedAt = schedule.updatedAt();
    }

    static RoutineScheduleJpaEntity from(RoutineSchedule schedule) {
        return new RoutineScheduleJpaEntity(schedule);
    }

    RoutineSchedule toDomain() {
        return new RoutineSchedule(userId, notificationTime, timezone, active,
                notificationEnabled, nextGenerationAt, createdAt, updatedAt);
    }
}
