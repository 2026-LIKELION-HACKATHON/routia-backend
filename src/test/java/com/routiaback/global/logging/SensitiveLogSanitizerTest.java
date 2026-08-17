package com.routiaback.global.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SensitiveLogSanitizerTest {

	@Test
	void masksEmailPasswordAndBearerToken() {
		String sanitized = SensitiveLogSanitizer.sanitize(
			"recipient=example@gmail.com password=secret passwordConfirm=confirmation "
				+ "OPENAI_API_KEY=api-key FIREBASE_SERVICE_ACCOUNT_BASE64=encoded "
				+ "Authorization: Bearer header.payload.signature"
		);

		assertThat(sanitized)
			.contains("ex***@gmail.com")
			.contains("password=***")
			.contains("passwordConfirm=***")
			.contains("OPENAI_API_KEY=***")
			.contains("FIREBASE_SERVICE_ACCOUNT_BASE64=***")
			.contains("Bearer ***")
			.doesNotContain("example@gmail.com", "secret", "confirmation", "api-key", "encoded",
				"header.payload.signature");
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
