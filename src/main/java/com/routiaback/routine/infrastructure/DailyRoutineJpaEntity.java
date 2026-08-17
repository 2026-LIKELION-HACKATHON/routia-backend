package com.routiaback.routine.infrastructure;

import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import com.routiaback.routine.domain.RoutineStatus;
import jakarta.persistence.*;
import java.time.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name="daily_routines", uniqueConstraints=@UniqueConstraint(name="uk_daily_routines_user_date",columnNames={"user_id","routine_date"}))
@Getter @NoArgsConstructor
class DailyRoutineJpaEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="user_id",nullable=false) private Long userId;
    @Column(name="weather_snapshot_id") private Long weatherSnapshotId;
    @Column(name="routine_date",nullable=false) private LocalDate routineDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private RoutineStatus status;
    @Column(name="direction_text",columnDefinition="TEXT") private String directionText;
    @Column(name="home_comment",length=1000) private String homeComment;
    @Enumerated(EnumType.STRING) @Column(name="difficulty_snapshot",nullable=false,length=30) private RoutineDifficulty difficultySnapshot;
    @Enumerated(EnumType.STRING) @Column(name="time_preference_snapshot",length=30) private RoutineTimePreference timePreferenceSnapshot;
    @Column(name="personalization_snapshot",columnDefinition="JSON") private String personalizationSnapshot;
    @Column(name="performance_snapshot",columnDefinition="JSON") private String performanceSnapshot;
    @Column(name="generation_error_code",length=50) private String generationErrorCode;
    @Column(name="ai_model",length=100) private String aiModel;
    @Column(name="prompt_version",length=50) private String promptVersion;
    @Column(name="generated_at") private Instant generatedAt;
    @Column(name="notification_scheduled_at") private Instant notificationScheduledAt;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    DailyRoutineJpaEntity(Long id,Long userId,Long weatherSnapshotId,LocalDate routineDate,RoutineStatus status,String directionText,String homeComment,RoutineDifficulty difficultySnapshot,RoutineTimePreference timePreferenceSnapshot,String personalizationSnapshot,String performanceSnapshot,String generationErrorCode,String aiModel,String promptVersion,Instant generatedAt,Instant notificationScheduledAt,Instant createdAt,Instant updatedAt){this.id=id;this.userId=userId;this.weatherSnapshotId=weatherSnapshotId;this.routineDate=routineDate;this.status=status;this.directionText=directionText;this.homeComment=homeComment;this.difficultySnapshot=difficultySnapshot;this.timePreferenceSnapshot=timePreferenceSnapshot;this.personalizationSnapshot=personalizationSnapshot;this.performanceSnapshot=performanceSnapshot;this.generationErrorCode=generationErrorCode;this.aiModel=aiModel;this.promptVersion=promptVersion;this.generatedAt=generatedAt;this.notificationScheduledAt=notificationScheduledAt;this.createdAt=createdAt;this.updatedAt=updatedAt;}
}
