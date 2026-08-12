package com.routiaback.auth.infrastructure;

import com.routiaback.auth.domain.VerificationPurpose;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "email_verifications")
class EmailVerificationJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private VerificationPurpose purpose;

	@Column(name = "code_hash", nullable = false)
	private String codeHash;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "verified_at")
	private Instant verifiedAt;

	@Column(name = "consumed_at")
	private Instant consumedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected EmailVerificationJpaEntity() {
	}

	EmailVerificationJpaEntity(Long id, String email, VerificationPurpose purpose, String codeHash, Instant expiresAt, Instant verifiedAt, Instant consumedAt, Instant createdAt) {
		this.id = id;
		this.email = email;
		this.purpose = purpose;
		this.codeHash = codeHash;
		this.expiresAt = expiresAt;
		this.verifiedAt = verifiedAt;
		this.consumedAt = consumedAt;
		this.createdAt = createdAt;
	}

	Long id() {
		return id;
	}

	String email() {
		return email;
	}

	VerificationPurpose purpose() {
		return purpose;
	}

	String codeHash() {
		return codeHash;
	}

	Instant expiresAt() {
		return expiresAt;
	}

	Instant verifiedAt() {
		return verifiedAt;
	}

	Instant consumedAt() {
		return consumedAt;
	}

	Instant createdAt() {
		return createdAt;
	}
}
