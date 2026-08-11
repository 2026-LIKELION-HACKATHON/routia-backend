package com.routiaback.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.routiaback.auth.application.command.EmailVerificationCodeCommand;
import com.routiaback.auth.application.command.EmailVerifyCommand;
import com.routiaback.auth.application.command.LoginCommand;
import com.routiaback.auth.application.command.SignupCommand;
import com.routiaback.auth.application.port.EmailSenderPort;
import com.routiaback.auth.application.port.EmailVerificationRepositoryPort;
import com.routiaback.auth.application.port.PasswordEncoderPort;
import com.routiaback.auth.application.port.TokenProviderPort;
import com.routiaback.auth.application.port.UserRepositoryPort;
import com.routiaback.auth.application.port.VerificationMailRendererPort;
import com.routiaback.auth.domain.AccountStatus;
import com.routiaback.auth.domain.EmailVerification;
import com.routiaback.auth.domain.User;
import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class AuthServiceTest {

	private final Clock clock = Clock.fixed(Instant.parse("2026-08-11T00:00:00Z"), ZoneOffset.UTC);
	private final FakeUserRepository users = new FakeUserRepository();
	private final FakeVerificationRepository verifications = new FakeVerificationRepository();
	private final FakeEmailSender emailSender = new FakeEmailSender();
	private final PasswordEncoderPort passwordEncoder = new FakePasswordEncoder();
	private final AuthService authService = new AuthService(
		users,
		verifications,
		emailSender,
		code -> "<html>" + code + "</html>",
		passwordEncoder,
		userId -> "token-" + userId,
		new EmailNormalizer(),
		() -> "123456",
		clock
	);

	@Test
	void issuesSignupVerificationCodeWithHashAndMail() {
		authService.issueSignupVerificationCode(new EmailVerificationCodeCommand(" User@Example.COM "));

		EmailVerification saved = verifications.saved.getFirst();
		assertThat(saved.email()).isEqualTo("user@example.com");
		assertThat(saved.matches("123456", passwordEncoder)).isTrue();
		assertThat(saved.expiresAt()).isEqualTo(Instant.parse("2026-08-11T00:05:00Z"));
		assertThat(emailSender.sentTo).isEqualTo("user@example.com");
		assertThat(emailSender.html).contains("123456");
	}

	@Test
	void mailFailureMarksVerificationConsumedAndFailsRequest() {
		emailSender.fail = true;

		assertThatThrownBy(() -> authService.issueSignupVerificationCode(new EmailVerificationCodeCommand("a@b.com")))
			.isInstanceOf(ApiException.class)
			.hasCauseInstanceOf(RuntimeException.class)
			.satisfies(exception -> assertThat(((ApiException) exception).getErrorCode())
				.isEqualTo(ErrorCode.EMAIL_SEND_FAILED));
		assertThat(verifications.saved.getFirst().consumedAt()).isNotNull();
	}

	@Test
	void verifiesOnlyLatestCode() {
		verifications.save(EmailVerification.issueSignup("user@example.com", passwordEncoder.encode("111111"), clock.instant(), clock.instant().plusSeconds(300)));
		authService.issueSignupVerificationCode(new EmailVerificationCodeCommand("user@example.com"));

		assertThatThrownBy(() -> authService.verifySignupEmail(new EmailVerifyCommand("user@example.com", "111111")))
			.isInstanceOf(ApiException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);

		authService.verifySignupEmail(new EmailVerifyCommand("user@example.com", "123456"));

		assertThat(verifications.latest().verifiedAt()).isEqualTo(clock.instant());
	}

	@Test
	void signupConsumesVerifiedEmailAndStoresPasswordHash() {
		EmailVerification verification = EmailVerification.issueSignup("user@example.com", passwordEncoder.encode("123456"), clock.instant(), clock.instant().plusSeconds(300));
		verification = verification.verify("123456", passwordEncoder, clock.instant());
		verifications.save(verification);

		authService.signup(new SignupCommand(" User@Example.COM ", "secret", "Soeun"));

		User saved = users.saved.getFirst();
		assertThat(saved.email()).isEqualTo("user@example.com");
		assertThat(saved.passwordHash()).isEqualTo("{fake}secret");
		assertThat(saved.accountStatus()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(saved.emailVerifiedAt()).isEqualTo(clock.instant());
		assertThat(verifications.latest().consumedAt()).isEqualTo(clock.instant());
	}

	@Test
	void signupKeepsVerificationTimeAndUsesCurrentTimeForUserCreation() {
		Instant verifiedAt = clock.instant();
		EmailVerification verification = EmailVerification.issueSignup(
			"user@example.com",
			passwordEncoder.encode("123456"),
			verifiedAt,
			verifiedAt.plusSeconds(300)
		).verify("123456", passwordEncoder, verifiedAt);
		verifications.save(verification);
		Instant signupAt = verifiedAt.plusSeconds(120);
		AuthService laterAuthService = authServiceAt(signupAt);

		laterAuthService.signup(new SignupCommand("user@example.com", "secret", "Soeun"));

		User saved = users.saved.getFirst();
		assertThat(saved.emailVerifiedAt()).isEqualTo(verifiedAt);
		assertThat(saved.createdAt()).isEqualTo(signupAt);
		assertThat(saved.updatedAt()).isEqualTo(signupAt);
	}

	@Test
	void mapsConcurrentDuplicateSaveToEmailAlreadyExistsWithoutConsumingVerification() {
		EmailVerification verification = EmailVerification.issueSignup(
			"user@example.com",
			passwordEncoder.encode("123456"),
			clock.instant(),
			clock.instant().plusSeconds(300)
		).verify("123456", passwordEncoder, clock.instant());
		verifications.save(verification);
		users.failOnSave = true;

		assertThatThrownBy(() -> authService.signup(new SignupCommand("user@example.com", "secret", "Soeun")))
			.isInstanceOf(ApiException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
		assertThat(verifications.latest().consumedAt()).isNull();
	}

	@Test
	void loginReturnsJwtAndUpdatesLastLoginAt() {
		users.save(User.create("user@example.com", passwordEncoder.encode("secret"), "Soeun", clock.instant()));

		String token = authService.login(new LoginCommand(" USER@example.com ", "secret")).accessToken();

		assertThat(token).isEqualTo("token-1");
		assertThat(users.saved.getFirst().lastLoginAt()).isEqualTo(clock.instant());
	}

	@Test
	void invalidPasswordDoesNotUpdateLastLoginTime() {
		users.save(User.create("user@example.com", passwordEncoder.encode("secret"), "Soeun", clock.instant()));

		assertThatThrownBy(() -> authService.login(new LoginCommand("user@example.com", "wrong")))
			.isInstanceOf(ApiException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_CREDENTIALS);
		assertThat(users.saved.getFirst().lastLoginAt()).isNull();
	}

	private AuthService authServiceAt(Instant instant) {
		return new AuthService(
			users,
			verifications,
			emailSender,
			code -> "<html>" + code + "</html>",
			passwordEncoder,
			userId -> "token-" + userId,
			new EmailNormalizer(),
			() -> "123456",
			Clock.fixed(instant, ZoneOffset.UTC)
		);
	}

	private static class FakeUserRepository implements UserRepositoryPort {
		private final List<User> saved = new ArrayList<>();
		private boolean failOnSave;

		@Override
		public boolean existsByEmail(String email) {
			return saved.stream().anyMatch(user -> user.email().equals(email));
		}

		@Override
		public Optional<User> findByEmail(String email) {
			return saved.stream().filter(user -> user.email().equals(email)).findFirst();
		}

		@Override
		public User save(User user) {
			if (failOnSave) {
				throw new DataIntegrityViolationException("duplicate email");
			}
			User savedUser = user.id() == null ? user.withId((long) saved.size() + 1) : user;
			saved.removeIf(existing -> existing.id().equals(savedUser.id()));
			saved.add(savedUser);
			return savedUser;
		}
	}

	private static class FakeVerificationRepository implements EmailVerificationRepositoryPort {
		private final List<EmailVerification> saved = new ArrayList<>();

		@Override
		public EmailVerification save(EmailVerification verification) {
			EmailVerification savedVerification = verification.id() == null ? verification.withId((long) saved.size() + 1) : verification;
			saved.removeIf(existing -> existing.id().equals(savedVerification.id()));
			saved.add(savedVerification);
			return savedVerification;
		}

		@Override
		public Optional<EmailVerification> findLatestSignupByEmail(String email) {
			return saved.stream()
				.filter(verification -> verification.email().equals(email))
				.max(Comparator.comparing(EmailVerification::createdAt).thenComparing(EmailVerification::id));
		}

		EmailVerification latest() {
			return findLatestSignupByEmail("user@example.com").orElseThrow();
		}
	}

	private static class FakeEmailSender implements EmailSenderPort {
		private boolean fail;
		private String sentTo;
		private String html;

		@Override
		public void send(String to, String subject, String html) {
			if (fail) {
				throw new IllegalStateException("mail failed");
			}
			this.sentTo = to;
			this.html = html;
		}
	}

	private interface FakeMailRenderer extends VerificationMailRendererPort {
	}

	private static class FakePasswordEncoder implements PasswordEncoderPort {
		@Override
		public String encode(String raw) {
			return "{fake}" + raw;
		}

		@Override
		public boolean matches(String raw, String encoded) {
			return encode(raw).equals(encoded);
		}
	}
}
