package com.routiaback.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserTest {

	private static final Instant NOW = Instant.parse("2026-08-11T00:00:00Z");

	@Test
	void allowsActiveUserAndRecordsLoginTime() {
		User active = user(AccountStatus.ACTIVE, null);

		assertThatCode(active::validateLoginAllowed).doesNotThrowAnyException();
		assertThat(active.recordLogin(NOW).lastLoginAt()).isEqualTo(NOW);
	}

	@Test
	void rejectsBlockedUser() {
		assertLoginRejected(user(AccountStatus.BLOCKED, null), ErrorCode.ACCOUNT_BLOCKED);
	}

	@Test
	void rejectsWithdrawnUser() {
		assertLoginRejected(user(AccountStatus.WITHDRAWN, null), ErrorCode.ACCOUNT_WITHDRAWN);
	}

	@Test
	void rejectsSoftDeletedUser() {
		assertLoginRejected(user(AccountStatus.ACTIVE, NOW), ErrorCode.ACCOUNT_WITHDRAWN);
	}

	private void assertLoginRejected(User user, ErrorCode errorCode) {
		assertThatThrownBy(user::validateLoginAllowed)
			.isInstanceOf(ApiException.class)
			.extracting("errorCode")
			.isEqualTo(errorCode);
	}

	private User user(AccountStatus status, Instant deletedAt) {
		return new User(1L, "user@example.com", "hash", "Soeun", status, NOW, null, NOW, NOW, deletedAt);
	}
}
