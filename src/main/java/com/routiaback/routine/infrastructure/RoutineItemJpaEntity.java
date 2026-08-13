package com.routiaback.routine.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "routine_items")
@Getter
@NoArgsConstructor
class RoutineItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long routineId;
    private String timeSlot;
    private String category;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String detail;

    private int sortOrder;

    @Column(name = "is_completed")
    private boolean completed;

    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public RoutineItemJpaEntity(Long id, Long routineId, String timeSlot, String category, String title,
                                String detail, int sortOrder, boolean completed, Instant completedAt,
                                Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.routineId = routineId;
        this.timeSlot = timeSlot;
        this.category = category;
        this.title = title;
        this.detail = detail;
        this.sortOrder = sortOrder;
        this.completed = completed;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}