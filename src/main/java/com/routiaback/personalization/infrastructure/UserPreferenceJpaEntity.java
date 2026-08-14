package com.routiaback.personalization.infrastructure;

import com.routiaback.personalization.domain.BodyGoal;
import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import com.routiaback.personalization.domain.SkinType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "user_preferences")
class UserPreferenceJpaEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "body_goal", length = 30)
    private BodyGoal bodyGoal;

    @Enumerated(EnumType.STRING)
    @Column(name = "skin_type", length = 30)
    private SkinType skinType;

    @Enumerated(EnumType.STRING)
    @Column(name = "routine_time_preference", length = 30)
    private RoutineTimePreference routineTimePreference;

    @Enumerated(EnumType.STRING)
    @Column(name = "routine_difficulty", length = 30)
    private RoutineDifficulty routineDifficulty;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserPreferenceJpaEntity() {
    }

    UserPreferenceJpaEntity(Long userId, BodyGoal bodyGoal, SkinType skinType,
            RoutineTimePreference routineTimePreference, RoutineDifficulty routineDifficulty,
            Instant createdAt, Instant updatedAt) {
        this.userId = userId;
        this.bodyGoal = bodyGoal;
        this.skinType = skinType;
        this.routineTimePreference = routineTimePreference;
        this.routineDifficulty = routineDifficulty;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    Long userId() { return userId; }
    BodyGoal bodyGoal() { return bodyGoal; }
    SkinType skinType() { return skinType; }
    RoutineTimePreference routineTimePreference() { return routineTimePreference; }
    RoutineDifficulty routineDifficulty() { return routineDifficulty; }
    Instant createdAt() { return createdAt; }
    Instant updatedAt() { return updatedAt; }
}
