package com.routiaback.auth.application.port;

public interface VerificationMailRendererPort {

	String render(String code);
}
