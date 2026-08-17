package com.routiaback.auth.infrastructure;

import com.routiaback.auth.application.port.EmailVerificationRepositoryPort;
import com.routiaback.auth.application.port.UserRepositoryPort;
import com.routiaback.auth.domain.EmailVerification;
import com.routiaback.auth.domain.User;
import com.routiaback.auth.domain.VerificationPurpose;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class AuthPersistenceAdapter implements UserRepositoryPort, EmailVerificationRepositoryPort {

	private final UserJpaRepository userJpaRepository;
	private final EmailVerificationJpaRepository verificationJpaRepository;

	AuthPersistenceAdapter(UserJpaRepository userJpaRepository, EmailVerificationJpaRepository verificationJpaRepository) {
		this.userJpaRepository = userJpaRepository;
		this.verificationJpaRepository = verificationJpaRepository;
	}

	@Override
	public boolean existsByEmail(String email) {
		return userJpaRepository.existsByEmail(email);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return userJpaRepository.findByEmail(email).map(this::toDomain);
	}

	@Override
	public User save(User user) {
		return toDomain(userJpaRepository.save(toEntity(user)));
	}

	@Override
	public EmailVerification save(EmailVerification verification) {
		return toDomain(verificationJpaRepository.save(toEntity(verification)));
	}

	@Override
	public Optional<EmailVerification> findLatestSignupByEmail(String email) {
		return verificationJpaRepository.findFirstByEmailAndPurposeOrderByCreatedAtDescIdDesc(email, VerificationPurpose.SIGNUP)
			.map(this::toDomain);
	}

	@Override
	public Optional<User> findById(Long id) {
		return userJpaRepository.findById(id).map(this::toDomain);
	}

	private UserJpaEntity toEntity(User user) {
		return new UserJpaEntity(user.id(), user.email(), user.passwordHash(), user.name(), user.accountStatus(), user.emailVerifiedAt(), user.lastLoginAt(), user.createdAt(), user.updatedAt(), user.deletedAt());
	}

	private User toDomain(UserJpaEntity entity) {
		return new User(entity.id(), entity.email(), entity.passwordHash(), entity.name(), entity.accountStatus(), entity.emailVerifiedAt(), entity.lastLoginAt(), entity.createdAt(), entity.updatedAt(), entity.deletedAt());
	}

	private EmailVerificationJpaEntity toEntity(EmailVerification verification) {
		return new EmailVerificationJpaEntity(verification.id(), verification.email(), verification.purpose(), verification.codeHash(), verification.expiresAt(), verification.verifiedAt(), verification.consumedAt(), verification.createdAt());
	}

	private EmailVerification toDomain(EmailVerificationJpaEntity entity) {
		return new EmailVerification(entity.id(), entity.email(), entity.purpose(), entity.codeHash(), entity.expiresAt(), entity.verifiedAt(), entity.consumedAt(), entity.createdAt());
	}
}
