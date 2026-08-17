package com.routiaback.auth.application.port;

public interface TokenProviderPort {

	String createAccessToken(Long userId);
}
