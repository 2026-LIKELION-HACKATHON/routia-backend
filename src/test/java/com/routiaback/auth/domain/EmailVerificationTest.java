package com.routiaback.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.routiaback.auth.application.port.PasswordEncoderPort;
import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class EmailVerificationTest {

	private static final Instant ISSUED_AT = Instant.parse("2026-08-11T00:00:00Z");
	private static final Instant EXPIRES_AT = Instant.parse("2026-08-11T00:05:00Z");
	private final PasswordEncoderPort passwordEncoder = new PlainTestPasswordEncoder();

	@Test
	void verifiesMatchingCodeBeforeExpiration() {
		EmailVerification verification = issue();

		EmailVerification verified = verification.verify("123456", passwordEncoder, ISSUED_AT.plusSeconds(299));

		assertThat(verified.verifiedAt()).isEqualTo(ISSUED_AT.plusSeconds(299));
		assertThat(verified.canBeUsedForSignup(ISSUED_AT.plusSeconds(299))).isTrue();
	}

	@Test
	void expiresAtTheExactExpirationTime() {
		EmailVerification verification = issue();

		assertThatThrownBy(() -> verification.verify("123456", passwordEncoder, EXPIRES_AT))
			.isInstanceOf(ApiException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
		assertThat(verification.canBeUsedForSignup(EXPIRES_AT)).isFalse();
	}

	@Test
	void rejectsMismatchedCode() {
		assertThatThrownBy(() -> issue().verify("999999", passwordEncoder, ISSUED_AT))
			.isInstanceOf(ApiException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
	}

	@Test
	void rejectsConsumedVerification() {
		EmailVerification consumed = issue().consume(ISSUED_AT.plusSeconds(30));

		assertThatThrownBy(() -> consumed.verify("123456", passwordEncoder, ISSUED_AT.plusSeconds(60)))
			.isInstanceOf(ApiException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);
	}

	private EmailVerification issue() {
		return EmailVerification.issueSignup("user@example.com", "123456", ISSUED_AT, EXPIRES_AT);
	}

	private static class PlainTestPasswordEncoder implements PasswordEncoderPort {
		@Override
		public String encode(String raw) {
			return raw;
		}

		@Override
		public boolean matches(String raw, String encoded) {
			return raw.equals(encoded);
		}
	}
}
