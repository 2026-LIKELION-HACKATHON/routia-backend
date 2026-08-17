package com.routiaback.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CorsConfigTest {

	@Test
	void parsesMultipleOriginsAndRemovesWhitespaceAndDuplicates() {
		assertThat(CorsConfig.parseAllowedOrigins(
			"https://a.example, https://b.example,https://a.example"))
			.containsExactly("https://a.example", "https://b.example");
	}

	@Test
	void acceptsTemporaryWildcardOriginPolicy() {
		assertThat(CorsConfig.parseAllowedOrigins("*")).containsExactly("*");
	}

	@Test
	void usesDenyAllPolicyWhenNoFrontendOriginIsConfigured() {
		assertThat(CorsConfig.parseAllowedOrigins("  ")).isEmpty();
	}
}
