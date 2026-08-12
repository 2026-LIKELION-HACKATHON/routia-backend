package com.routiaback.auth.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmailNormalizerTest {

	private final EmailNormalizer normalizer = new EmailNormalizer();

	@Test
	void trimsAndLowercasesEmailAtApplicationBoundary() {
		assertThat(normalizer.normalize("  User@Example.COM  ")).isEqualTo("user@example.com");
	}
}
