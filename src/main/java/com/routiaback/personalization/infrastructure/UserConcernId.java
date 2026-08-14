package com.routiaback.personalization.infrastructure;

import java.io.Serializable;
import java.util.Objects;

public class UserConcernId implements Serializable {

    private Long userId;
    private String concernCode;

    public UserConcernId() {
    }

    public UserConcernId(Long userId, String concernCode) {
        this.userId = userId;
        this.concernCode = concernCode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof UserConcernId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(concernCode, that.concernCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, concernCode);
    }
}
