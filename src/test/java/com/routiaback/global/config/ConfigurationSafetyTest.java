package com.routiaback.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ConfigurationSafetyTest {

	@Test
	void requiresJwtSecretFromEnvironmentWithoutHardcodedFallback() throws Exception {
		String properties = new ClassPathResource("application.properties")
			.getContentAsString(StandardCharsets.UTF_8);

		assertThat(properties).contains("routia.jwt.secret=${JWT_SECRET}");
		assertThat(properties).doesNotContain("dev-secret");
	}
}
