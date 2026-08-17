package com.routiaback.personalization.infrastructure;

import java.io.Serializable;
import java.util.Objects;

public class UserSelectionId implements Serializable {
    private Long userId;
    private String code;
    public UserSelectionId() { }
    public UserSelectionId(Long userId, String code) { this.userId = userId; this.code = code; }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof UserSelectionId that
                && Objects.equals(userId, that.userId) && Objects.equals(code, that.code);
    }
    @Override public int hashCode() { return Objects.hash(userId, code); }
}
