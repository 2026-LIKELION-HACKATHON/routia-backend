package com.routiaback.auth.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SecureRandomVerificationCodeGeneratorTest {

	@Test
	void generatesSixDigitNumericCode() {
		VerificationCodeGenerator generator = new SecureRandomVerificationCodeGenerator();

		String code = generator.generate();

		assertThat(code).matches("\\d{6}");
	}
}
