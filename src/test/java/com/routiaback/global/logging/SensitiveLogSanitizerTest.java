package com.routiaback.global.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SensitiveLogSanitizerTest {

	@Test
	void masksEmailPasswordAndBearerToken() {
		String sanitized = SensitiveLogSanitizer.sanitize(
			"recipient=example@gmail.com password=secret Authorization: Bearer header.payload.signature"
		);

		assertThat(sanitized)
			.contains("ex***@gmail.com")
			.contains("password=***")
			.contains("Bearer ***")
			.doesNotContain("example@gmail.com", "secret", "header.payload.signature");
	}

	@Test
	void findsDeepestCauseAndSanitizesStackTrace() {
		IllegalStateException root = new IllegalStateException("mail failed for example@gmail.com password=secret");
		RuntimeException wrapper = new RuntimeException("wrapper", root);

		assertThat(SensitiveLogSanitizer.rootCause(wrapper)).isSameAs(root);
		assertThat(SensitiveLogSanitizer.stackTrace(wrapper))
			.contains("ex***@gmail.com", "password=***")
			.doesNotContain("example@gmail.com", "secret");
	}
}
