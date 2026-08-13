package com.routiaback.routine.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "daily_routines")
@Getter
@NoArgsConstructor
class DailyRoutineJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private LocalDate routineDate;
    private String status;

    @Column(columnDefinition = "TEXT")
    private String directionText;

    private String homeComment;
    private Instant createdAt;
    private Instant updatedAt;

    public DailyRoutineJpaEntity(Long id, Long userId, LocalDate routineDate, String status,
                                 String directionText, String homeComment, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.routineDate = routineDate;
        this.status = status;
        this.directionText = directionText;
        this.homeComment = homeComment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}