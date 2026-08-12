package com.routiaback.auth.infrastructure;

import com.routiaback.auth.application.port.PasswordEncoderPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class SpringPasswordEncoderAdapter implements PasswordEncoderPort {

	private final PasswordEncoder passwordEncoder;

	SpringPasswordEncoderAdapter(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public String encode(String raw) {
		return passwordEncoder.encode(raw);
	}

	@Override
	public boolean matches(String raw, String encoded) {
		return passwordEncoder.matches(raw, encoded);
	}
}
