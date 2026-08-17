package com.routiaback.personalization.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(UserSelectionId.class)
@Table(name = "user_body_goals")
class UserBodyGoalJpaEntity {
    @Id @Column(name = "user_id")
    private Long userId;
    @Id @Column(name = "code", length = 30)
    private String code;
    protected UserBodyGoalJpaEntity() { }
    UserBodyGoalJpaEntity(Long userId, String code) { this.userId = userId; this.code = code; }
}
