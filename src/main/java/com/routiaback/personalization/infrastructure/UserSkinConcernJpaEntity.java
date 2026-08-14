package com.routiaback.personalization.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(UserConcernId.class)
@Table(name = "user_skin_concerns")
class UserSkinConcernJpaEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "concern_code", length = 30)
    private String concernCode;

    protected UserSkinConcernJpaEntity() {
    }

    UserSkinConcernJpaEntity(Long userId, String concernCode) {
        this.userId = userId;
        this.concernCode = concernCode;
    }
}
