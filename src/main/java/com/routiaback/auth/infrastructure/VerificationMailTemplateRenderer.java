package com.routiaback.auth.infrastructure;

import com.routiaback.auth.application.port.VerificationMailRendererPort;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class VerificationMailTemplateRenderer implements VerificationMailRendererPort {

	private static final ClassPathResource TEMPLATE =
		new ClassPathResource("templates/mail/verification-code.html");

	@Override
	public String render(String code) {
		try {
			return TEMPLATE.getContentAsString(StandardCharsets.UTF_8)
				.replace("{{verificationCode}}", code);
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to load verification mail template", exception);
		}
	}
}
