package com.routiaback.auth.infrastructure;

import com.routiaback.auth.application.port.EmailSenderPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
class SmtpEmailSenderAdapter implements EmailSenderPort {

	private final JavaMailSender mailSender;
	private final String from;

	SmtpEmailSenderAdapter(JavaMailSender mailSender, @Value("${routia.mail.from}") String from) {
		this.mailSender = mailSender;
		this.from = from;
	}

	@Override
	public void send(String to, String subject, String html) {
		mailSender.send(message -> {
			MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(html, true);
		});
	}
}
