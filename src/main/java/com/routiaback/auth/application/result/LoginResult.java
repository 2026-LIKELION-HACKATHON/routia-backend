package com.routiaback.auth.application.result;

public record LoginResult(String accessToken, String tokenType) {

	public LoginResult(String accessToken) {
		this(accessToken, "Bearer");
	}
}
