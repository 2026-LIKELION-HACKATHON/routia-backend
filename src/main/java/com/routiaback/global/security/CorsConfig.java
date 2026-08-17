package com.routiaback.global.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	private final List<String> allowedOrigins;

	public CorsConfig(@Value("${routia.cors.allowed-origins}") String allowedOrigins) {
		this.allowedOrigins = parseAllowedOrigins(allowedOrigins);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(allowedOrigins);
		configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
		configuration.setAllowCredentials(false);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	static List<String> parseAllowedOrigins(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalStateException("At least one CORS allowed origin must be configured");
		}
		List<String> origins = Arrays.stream(value.split(","))
			.map(String::trim)
			.filter(origin -> !origin.isEmpty())
			.distinct()
			.toList();
		if (origins.isEmpty() || origins.contains("*")) {
			throw new IllegalStateException("CORS allowed origins must be an explicit allowlist");
		}
		return origins;
	}
}
