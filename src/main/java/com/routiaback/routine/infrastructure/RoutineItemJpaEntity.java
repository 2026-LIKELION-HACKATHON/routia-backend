package com.routiaback.routine.infrastructure;

import jakarta.persistence.*; import java.time.Instant; import lombok.*;
@Entity @Table(name="routine_items") @Getter @NoArgsConstructor
class RoutineItemJpaEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="routine_id",nullable=false) private Long routineId;
    @Column(name="time_slot",nullable=false,length=20) private String timeSlot;
    @Column(nullable=false,length=20) private String category;
    @Column(nullable=false,length=150) private String title;
    @Column(columnDefinition="TEXT") private String detail;
    @Column(name="effect_code",length=30) private String effectCode;
    @Column(name="expected_effect",length=255) private String expectedEffect;
    @Column(name="sort_order",nullable=false) private int sortOrder;
    @Column(name="is_completed",nullable=false) private boolean completed;
    @Column(name="completed_at") private Instant completedAt;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    RoutineItemJpaEntity(Long id,Long routineId,String timeSlot,String category,String title,String detail,String effectCode,String expectedEffect,int sortOrder,boolean completed,Instant completedAt,Instant createdAt,Instant updatedAt){this.id=id;this.routineId=routineId;this.timeSlot=timeSlot;this.category=category;this.title=title;this.detail=detail;this.effectCode=effectCode;this.expectedEffect=expectedEffect;this.sortOrder=sortOrder;this.completed=completed;this.completedAt=completedAt;this.createdAt=createdAt;this.updatedAt=updatedAt;}
}
