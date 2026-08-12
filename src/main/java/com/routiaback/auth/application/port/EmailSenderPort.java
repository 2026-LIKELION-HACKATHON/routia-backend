package com.routiaback.auth.application.port;

public interface EmailSenderPort {

	void send(String to, String subject, String html);
}
