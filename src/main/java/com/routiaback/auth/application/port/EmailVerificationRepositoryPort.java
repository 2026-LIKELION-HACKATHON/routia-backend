package com.routiaback.auth.application.port;

import com.routiaback.auth.domain.EmailVerification;
import java.util.Optional;

public interface EmailVerificationRepositoryPort {

	EmailVerification save(EmailVerification verification);

	Optional<EmailVerification> findLatestSignupByEmail(String email);
}
