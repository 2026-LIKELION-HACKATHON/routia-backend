package com.routiaback.auth.domain;

import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import java.time.Instant;

public record User(
	Long id,
	String email,
	String passwordHash,
	String name,
	AccountStatus accountStatus,
	Instant emailVerifiedAt,
	Instant lastLoginAt,
	Instant createdAt,
	Instant updatedAt,
	Instant deletedAt
) {

	public static User create(String email, String passwordHash, String name, Instant verifiedAt) {
		return create(email, passwordHash, name, verifiedAt, verifiedAt);
	}

	public static User create(String email, String passwordHash, String name, Instant verifiedAt, Instant createdAt) {
		return new User(null, email, passwordHash, name, AccountStatus.ACTIVE, verifiedAt, null, createdAt, createdAt, null);
	}

	public User withId(Long id) {
		return new User(id, email, passwordHash, name, accountStatus, emailVerifiedAt, lastLoginAt, createdAt, updatedAt, deletedAt);
	}

	public User recordLogin(Instant loggedInAt) {
		return new User(id, email, passwordHash, name, accountStatus, emailVerifiedAt, loggedInAt, createdAt, loggedInAt, deletedAt);
	}

	public void validateLoginAllowed() {
		if (deletedAt != null || accountStatus == AccountStatus.WITHDRAWN) {
			throw new ApiException(ErrorCode.ACCOUNT_WITHDRAWN);
		}
		if (accountStatus == AccountStatus.BLOCKED) {
			throw new ApiException(ErrorCode.ACCOUNT_BLOCKED);
		}
	}
}
