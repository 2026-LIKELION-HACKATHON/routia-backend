package com.routiaback.global.security;

import com.routiaback.auth.application.port.TokenProviderPort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements TokenProviderPort {

	private final String secret;
	private final long accessExpirationSeconds;
	private final Clock clock;

	public JwtTokenProvider(
		@Value("${routia.jwt.secret}") String secret,
		@Value("${routia.jwt.access-expiration}") long accessExpirationSeconds,
		Clock clock
	) {
		this.secret = secret;
		this.accessExpirationSeconds = accessExpirationSeconds;
		this.clock = clock;
	}

	@Override
	public String createAccessToken(Long userId) {
		Instant now = clock.instant();
		String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
		String payload = base64Url("""
			{"sub":"%s","iat":%d,"exp":%d}
			""".formatted(userId, now.getEpochSecond(), now.plusSeconds(accessExpirationSeconds).getEpochSecond()).trim());
		String unsigned = header + "." + payload;
		return unsigned + "." + sign(unsigned);
	}

	public Long parseUserId(String token) {
		String[] parts = token.split("\\.");
		if (parts.length != 3 || !hasValidSignature(parts[0] + "." + parts[1], parts[2])) {
			throw new IllegalArgumentException("Invalid JWT");
		}
		String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
		long exp = Long.parseLong(extractJsonStringOrNumber(payload, "exp"));
		if (clock.instant().getEpochSecond() >= exp) {
			throw new IllegalArgumentException("Expired JWT");
		}
		return Long.parseLong(extractJsonStringOrNumber(payload, "sub"));
	}

	private boolean hasValidSignature(String unsignedToken, String signature) {
		return MessageDigest.isEqual(
			sign(unsignedToken).getBytes(StandardCharsets.US_ASCII),
			signature.getBytes(StandardCharsets.US_ASCII)
		);
	}

	private String sign(String value) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to sign JWT", ex);
		}
	}

	private String base64Url(String value) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	private String extractJsonStringOrNumber(String json, String key) {
		String marker = "\"" + key + "\":";
		int start = json.indexOf(marker);
		if (start < 0) {
			throw new IllegalArgumentException("Missing claim");
		}
		start += marker.length();
		if (json.charAt(start) == '"') {
			int end = json.indexOf('"', start + 1);
			return json.substring(start + 1, end);
		}
		int end = start;
		while (end < json.length() && Character.isDigit(json.charAt(end))) {
			end++;
		}
		return json.substring(start, end);
	}
}
