package com.routiaback.auth.application;

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
import com.routiaback.auth.application.result.EmailDuplicateCheckResult;
import com.routiaback.auth.application.result.LoginResult;
import com.routiaback.auth.domain.EmailVerification;
import com.routiaback.auth.domain.User;
import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private static final String VERIFICATION_SUBJECT = "[Routia] 이메일 인증번호 안내";

	private final UserRepositoryPort userRepository;
	private final EmailVerificationRepositoryPort verificationRepository;
	private final EmailSenderPort emailSender;
	private final VerificationMailRendererPort mailRenderer;
	private final PasswordEncoderPort passwordEncoder;
	private final TokenProviderPort tokenProvider;
	private final EmailNormalizer emailNormalizer;
	private final VerificationCodeGenerator codeGenerator;
	private final Clock clock;

	public AuthService(
		UserRepositoryPort userRepository,
		EmailVerificationRepositoryPort verificationRepository,
		EmailSenderPort emailSender,
		VerificationMailRendererPort mailRenderer,
		PasswordEncoderPort passwordEncoder,
		TokenProviderPort tokenProvider,
		EmailNormalizer emailNormalizer,
		VerificationCodeGenerator codeGenerator,
		Clock clock
	) {
		this.userRepository = userRepository;
		this.verificationRepository = verificationRepository;
		this.emailSender = emailSender;
		this.mailRenderer = mailRenderer;
		this.passwordEncoder = passwordEncoder;
		this.tokenProvider = tokenProvider;
		this.emailNormalizer = emailNormalizer;
		this.codeGenerator = codeGenerator;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public EmailDuplicateCheckResult checkDuplicate(String email) {
		return new EmailDuplicateCheckResult(userRepository.existsByEmail(emailNormalizer.normalize(email)));
	}

	@Transactional
	public void issueSignupVerificationCode(EmailVerificationCodeCommand command) {
		String email = emailNormalizer.normalize(command.email());
		Instant now = clock.instant();
		String code = codeGenerator.generate();
		EmailVerification verification = verificationRepository.save(
			EmailVerification.issueSignup(email, passwordEncoder.encode(code), now, now.plusSeconds(300))
		);

		try {
			emailSender.send(email, VERIFICATION_SUBJECT, mailRenderer.render(code));
		} catch (RuntimeException ex) {
			// SMTP cannot be rolled back with the DB. Consuming the row prevents a never-delivered code from being accepted later.
			verificationRepository.save(verification.failDelivery(now));
			throw new ApiException(ErrorCode.EMAIL_SEND_FAILED);
		}
	}

	@Transactional
	public void verifySignupEmail(EmailVerifyCommand command) {
		String email = emailNormalizer.normalize(command.email());
		EmailVerification verification = latestVerification(email);
		verificationRepository.save(verification.verify(command.code(), passwordEncoder, clock.instant()));
	}

	@Transactional
	public void signup(SignupCommand command) {
		String email = emailNormalizer.normalize(command.email());
		if (userRepository.existsByEmail(email)) {
			throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}

		Instant now = clock.instant();
		EmailVerification verification = latestVerification(email);
		if (!verification.canBeUsedForSignup(now)) {
			throw new ApiException(ErrorCode.EMAIL_NOT_VERIFIED);
		}

		try {
			userRepository.save(User.create(
				email,
				passwordEncoder.encode(command.password()),
				command.name(),
				verification.verifiedAt(),
				now
			));
			verificationRepository.save(verification.consume(now));
		} catch (DataIntegrityViolationException ex) {
			throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}
	}

	@Transactional
	public LoginResult login(LoginCommand command) {
		String email = emailNormalizer.normalize(command.email());
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS));
		user.validateLoginAllowed();
		if (!passwordEncoder.matches(command.password(), user.passwordHash())) {
			throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
		}

		User saved = userRepository.save(user.recordLogin(clock.instant()));
		return new LoginResult(tokenProvider.createAccessToken(saved.id()));
	}

	private EmailVerification latestVerification(String email) {
		return verificationRepository.findLatestSignupByEmail(email)
			.orElseThrow(() -> new ApiException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));
	}
}
