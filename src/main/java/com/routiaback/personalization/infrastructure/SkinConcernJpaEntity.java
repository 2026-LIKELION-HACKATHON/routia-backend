package com.routiaback.personalization.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "skin_concerns")
class SkinConcernJpaEntity {

    @Id
    @Column(length = 30)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
    @Column(nullable = false)
    private boolean active;

    protected SkinConcernJpaEntity() {
    }

    String code() { return code; }
    String name() { return name; }
}
