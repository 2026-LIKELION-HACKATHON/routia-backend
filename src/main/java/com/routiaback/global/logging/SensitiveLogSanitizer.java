package com.routiaback.global.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SensitiveLogSanitizer {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("([A-Za-z0-9._%+-]+)@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})");
	private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(password|passwd|pwd)(\\s*[=:]\\s*)[^\\s,;]+", Pattern.MULTILINE);
	private static final Pattern BEARER_PATTERN = Pattern.compile("(?i)(bearer\\s+)[A-Za-z0-9._~+/-]+=*");

	private SensitiveLogSanitizer() {
	}

	public static String maskEmail(String email) {
		if (email == null || email.isBlank()) {
			return "<empty>";
		}
		Matcher matcher = EMAIL_PATTERN.matcher(email.trim());
		if (!matcher.matches()) {
			return "<invalid-email>";
		}
		String localPart = matcher.group(1);
		int visibleLength = Math.min(2, localPart.length());
		return localPart.substring(0, visibleLength) + "***@" + matcher.group(2);
	}

	public static String sanitize(String value) {
		if (value == null) {
			return "<none>";
		}
		String maskedEmails = EMAIL_PATTERN.matcher(value).replaceAll(matchResult ->
			maskEmail(matchResult.group())
		);
		String maskedPasswords = PASSWORD_PATTERN.matcher(maskedEmails).replaceAll("$1$2***");
		return BEARER_PATTERN.matcher(maskedPasswords).replaceAll("$1***");
	}

	public static Throwable rootCause(Throwable throwable) {
		Throwable current = throwable;
		while (current.getCause() != null && current.getCause() != current) {
			current = current.getCause();
		}
		return current;
	}

	public static String stackTrace(Throwable throwable) {
		StringWriter writer = new StringWriter();
		throwable.printStackTrace(new PrintWriter(writer));
		return sanitize(writer.toString());
	}
}
