package com.routiaback.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.reset;

import com.routiaback.auth.application.AuthService;
import com.routiaback.auth.application.command.EmailVerificationCodeCommand;
import com.routiaback.auth.application.port.EmailSenderPort;
import com.routiaback.auth.domain.EmailVerification;
import com.routiaback.auth.domain.User;
import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
	"spring.jpa.hibernate.ddl-auto=validate",
	"routia.jwt.secret=test-secret-that-is-at-least-32-bytes-long"
})
@Testcontainers(disabledWithoutDocker = true)
class AuthPersistenceAdapterMySqlTest {

	@Container
	private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
		.withDatabaseName("routia")
		.withUsername("routia")
		.withPassword("routia")
		.withInitScript("db/auth-schema.sql");

	@Autowired
	private AuthPersistenceAdapter persistenceAdapter;

	@Autowired
	private AuthService authService;

	@MockitoBean
	private EmailSenderPort emailSender;

	@Autowired
	private EmailVerificationJpaRepository verificationJpaRepository;

	@Autowired
	private UserJpaRepository userJpaRepository;

	@DynamicPropertySource
	static void databaseProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
		registry.add("spring.datasource.username", MYSQL::getUsername);
		registry.add("spring.datasource.password", MYSQL::getPassword);
	}

	@BeforeEach
	void cleanDatabase() {
		reset(emailSender);
		verificationJpaRepository.deleteAll();
		userJpaRepository.deleteAll();
	}

	@Test
	void persistsUserWithMySqlDatetimeMapping() {
		Instant verifiedAt = Instant.parse("2026-08-11T00:00:00.123456Z");

		User saved = persistenceAdapter.save(
			User.create("user@example.com", "password-hash", "Soeun", verifiedAt)
		);

		assertThat(saved.id()).isNotNull();
		assertThat(saved.email()).isEqualTo("user@example.com");
		assertThat(saved.emailVerifiedAt()).isEqualTo(verifiedAt);
		assertThat(saved.createdAt()).isEqualTo(verifiedAt);
	}

	@Test
	void enforcesUniqueEmailConstraint() {
		Instant now = Instant.parse("2026-08-11T00:00:00Z");
		persistenceAdapter.save(User.create("user@example.com", "hash-1", "Soeun", now));

		assertThatThrownBy(() -> persistenceAdapter.save(
			User.create("user@example.com", "hash-2", "Another", now)
		))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void findsLatestSignupVerificationByCreatedTimeAndId() {
		Instant now = Instant.parse("2026-08-11T00:00:00Z");
		EmailVerification older = persistenceAdapter.save(
			EmailVerification.issueSignup("user@example.com", "old-hash", now, now.plusSeconds(300))
		);
		EmailVerification latest = persistenceAdapter.save(
			EmailVerification.issueSignup("user@example.com", "latest-hash", now, now.plusSeconds(300))
		);

		EmailVerification found = persistenceAdapter.findLatestSignupByEmail("user@example.com").orElseThrow();

		assertThat(found.id()).isEqualTo(latest.id());
		assertThat(found.id()).isGreaterThan(older.id());
		assertThat(found.codeHash()).isEqualTo("latest-hash");
	}

	@Test
	void commitsFailedMailVerificationAsConsumed() {
		willThrow(new IllegalStateException("smtp unavailable"))
			.given(emailSender).send(anyString(), anyString(), anyString());

		assertThatThrownBy(() -> authService.issueSignupVerificationCode(
			new EmailVerificationCodeCommand("user@example.com")
		))
			.isInstanceOf(ApiException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.EMAIL_SEND_FAILED);

		EmailVerification failed = persistenceAdapter
			.findLatestSignupByEmail("user@example.com")
			.orElseThrow();
		assertThat(failed.consumedAt()).isNotNull();
		assertThat(failed.verifiedAt()).isNull();
	}
}
