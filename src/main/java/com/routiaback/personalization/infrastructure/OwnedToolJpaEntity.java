package com.routiaback.personalization.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "owned_tools")
class OwnedToolJpaEntity {
    @Id @Column(length = 40)
    private String code;
    @Column(nullable = false, length = 80)
    private String name;
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
    @Column(nullable = false)
    private boolean active;
    protected OwnedToolJpaEntity() { }
    String code() { return code; }
    String name() { return name; }
}
