package com.routiaback.auth.application.port;

public interface PasswordEncoderPort {

	String encode(String raw);

	boolean matches(String raw, String encoded);
}
