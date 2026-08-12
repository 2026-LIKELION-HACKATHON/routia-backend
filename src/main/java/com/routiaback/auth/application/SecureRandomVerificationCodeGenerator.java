package com.routiaback.auth.application;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class SecureRandomVerificationCodeGenerator implements VerificationCodeGenerator {

	private final SecureRandom secureRandom = new SecureRandom();

	@Override
	public String generate() {
		return "%06d".formatted(secureRandom.nextInt(1_000_000));
	}
}
