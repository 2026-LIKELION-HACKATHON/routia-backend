package com.routiaback.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class VerificationMailTemplateRendererTest {

	@Test
	void keepsVerificationMailAsClasspathHtmlTemplate() {
		ClassPathResource template = new ClassPathResource("templates/mail/verification-code.html");

		assertThat(template.exists()).isTrue();
	}

	@Test
	void rendersVerificationCodeAndExpiryMessage() {
		VerificationMailTemplateRenderer renderer = new VerificationMailTemplateRenderer();

		String html = renderer.render("123456");

		assertThat(html).contains("Routia");
		assertThat(html).contains("123456");
		assertThat(html).contains("5분");
	}
}
