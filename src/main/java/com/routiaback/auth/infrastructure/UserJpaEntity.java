package com.routiaback.auth.infrastructure;

import com.routiaback.auth.domain.AccountStatus;
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
@Table(name = "users")
class UserJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(nullable = false, length = 50)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "account_status", nullable = false, length = 20)
	private AccountStatus accountStatus;

	@Column(name = "email_verified_at")
	private Instant emailVerifiedAt;

	@Column(name = "last_login_at")
	private Instant lastLoginAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	protected UserJpaEntity() {
	}

	UserJpaEntity(Long id, String email, String passwordHash, String name, AccountStatus accountStatus, Instant emailVerifiedAt, Instant lastLoginAt, Instant createdAt, Instant updatedAt, Instant deletedAt) {
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.name = name;
		this.accountStatus = accountStatus;
		this.emailVerifiedAt = emailVerifiedAt;
		this.lastLoginAt = lastLoginAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}

	Long id() {
		return id;
	}

	String email() {
		return email;
	}

	String passwordHash() {
		return passwordHash;
	}

	String name() {
		return name;
	}

	AccountStatus accountStatus() {
		return accountStatus;
	}

	Instant emailVerifiedAt() {
		return emailVerifiedAt;
	}

	Instant lastLoginAt() {
		return lastLoginAt;
	}

	Instant createdAt() {
		return createdAt;
	}

	Instant updatedAt() {
		return updatedAt;
	}

	Instant deletedAt() {
		return deletedAt;
	}
}
