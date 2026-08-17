package com.routiaback.personalization.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "body_goals")
class BodyGoalJpaEntity {
    @Id @Column(length = 30)
    private String code;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
    @Column(nullable = false)
    private boolean active;
    protected BodyGoalJpaEntity() { }
    String code() { return code; }
    String name() { return name; }
}
