package com.routiaback.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VerificationMailTemplateRendererTest {

	@Test
	void rendersVerificationCodeAndExpiryMessage() {
		VerificationMailTemplateRenderer renderer = new VerificationMailTemplateRenderer();

		String html = renderer.render("123456");

		assertThat(html).contains("Routia");
		assertThat(html).contains("123456");
		assertThat(html).contains("5분");
	}
}
