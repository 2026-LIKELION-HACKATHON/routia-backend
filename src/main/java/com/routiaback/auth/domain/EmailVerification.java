package com.routiaback.auth.domain;

import com.routiaback.auth.application.port.PasswordEncoderPort;
import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import java.time.Instant;

public record EmailVerification(
	Long id,
	String email,
	VerificationPurpose purpose,
	String codeHash,
	Instant expiresAt,
	Instant verifiedAt,
	Instant consumedAt,
	Instant createdAt
) {

	public static EmailVerification issueSignup(String email, String codeHash, Instant now, Instant expiresAt) {
		return new EmailVerification(null, email, VerificationPurpose.SIGNUP, codeHash, expiresAt, null, null, now);
	}

	public EmailVerification withId(Long id) {
		return new EmailVerification(id, email, purpose, codeHash, expiresAt, verifiedAt, consumedAt, createdAt);
	}

	public boolean matches(String code, PasswordEncoderPort passwordEncoder) {
		return passwordEncoder.matches(code, codeHash);
	}

	public EmailVerification verify(String code, PasswordEncoderPort passwordEncoder, Instant now) {
		if (consumedAt != null) {
			throw new ApiException(ErrorCode.EMAIL_NOT_VERIFIED);
		}
		if (!now.isBefore(expiresAt)) {
			throw new ApiException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
		}
		if (!matches(code, passwordEncoder)) {
			throw new ApiException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
		}
		return new EmailVerification(id, email, purpose, codeHash, expiresAt, now, consumedAt, createdAt);
	}

	public EmailVerification consume(Instant now) {
		return new EmailVerification(id, email, purpose, codeHash, expiresAt, verifiedAt, now, createdAt);
	}

	public EmailVerification failDelivery(Instant now) {
		return consume(now);
	}

	public boolean canBeUsedForSignup(Instant now) {
		return verifiedAt != null && consumedAt == null && now.isBefore(expiresAt);
	}
}
