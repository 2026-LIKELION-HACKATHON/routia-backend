package com.routiaback.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CorsConfigTest {

	@Test
	void parsesMultipleOriginsAndRemovesWhitespaceAndDuplicates() {
		assertThat(CorsConfig.parseAllowedOrigins(
			"https://a.example, https://b.example,https://a.example"))
			.containsExactly("https://a.example", "https://b.example");
	}

	@Test
	void rejectsWildcardOrigin() {
		assertThatThrownBy(() -> CorsConfig.parseAllowedOrigins("*"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("explicit allowlist");
	}

	@Test
	void rejectsEmptyOriginConfiguration() {
		assertThatThrownBy(() -> CorsConfig.parseAllowedOrigins("  "))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("At least one");
	}
}
