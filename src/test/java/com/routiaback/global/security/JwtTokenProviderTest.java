package com.routiaback.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

	private static final String SECRET = "test-secret-that-is-at-least-32-bytes-long";
	private static final Instant ISSUED_AT = Instant.parse("2026-08-11T00:00:00Z");

	@Test
	void createsSignedAccessTokenWithOnlyUserIdAsSubject() {
		JwtTokenProvider provider = providerAt(ISSUED_AT, 3600);

		String token = provider.createAccessToken(42L);
		String payload = decodePayload(token);

		assertThat(provider.parseUserId(token)).isEqualTo(42L);
		assertThat(payload).contains("\"sub\":\"42\"");
		assertThat(payload).doesNotContain("email", "password", "name");
	}

	@Test
	void rejectsTamperedSignature() {
		JwtTokenProvider provider = providerAt(ISSUED_AT, 3600);
		String token = provider.createAccessToken(42L);
		String tampered = token.substring(0, token.length() - 1) + "x";

		assertThatThrownBy(() -> provider.parseUserId(tampered))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void expiresAtTheExactExpirationTime() {
		String token = providerAt(ISSUED_AT, 60).createAccessToken(42L);
		JwtTokenProvider providerAtExpiration = providerAt(ISSUED_AT.plusSeconds(60), 60);

		assertThatThrownBy(() -> providerAtExpiration.parseUserId(token))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Expired JWT");
	}

	@Test
	void rejectsTokenSignedWithAnotherSecret() {
		String token = providerAt(ISSUED_AT, 3600).createAccessToken(42L);
		JwtTokenProvider anotherProvider = new JwtTokenProvider(
			"another-test-secret-that-is-also-32-bytes",
			3600,
			Clock.fixed(ISSUED_AT, ZoneOffset.UTC)
		);

		assertThatThrownBy(() -> anotherProvider.parseUserId(token))
			.isInstanceOf(IllegalArgumentException.class);
	}

	private JwtTokenProvider providerAt(Instant instant, long expirationSeconds) {
		return new JwtTokenProvider(SECRET, expirationSeconds, Clock.fixed(instant, ZoneOffset.UTC));
	}

	private String decodePayload(String token) {
		String payload = token.split("\\.")[1];
		return new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
	}
}
