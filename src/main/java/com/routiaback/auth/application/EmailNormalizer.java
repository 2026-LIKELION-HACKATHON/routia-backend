package com.routiaback.auth.application;

import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class EmailNormalizer {

	public String normalize(String email) {
		return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
	}
}
