package com.routiaback.personalization.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(UserSelectionId.class)
@Table(name = "user_owned_tools")
class UserOwnedToolJpaEntity {
    @Id @Column(name = "user_id")
    private Long userId;
    @Id @Column(name = "code", length = 40)
    private String code;
    protected UserOwnedToolJpaEntity() { }
    UserOwnedToolJpaEntity(Long userId, String code) { this.userId = userId; this.code = code; }
}
