package com.routiaback.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

class SmtpEmailSenderAdapterTest {

	@Test
	void preparesHtmlVerificationMailWithoutConnectingToSmtp() throws Exception {
		JavaMailSender mailSender = mock(JavaMailSender.class);
		MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
		doAnswer(invocation -> {
			MimeMessagePreparator preparator = invocation.getArgument(0);
			preparator.prepare(message);
			message.saveChanges();
			return null;
		}).when(mailSender).send(any(MimeMessagePreparator.class));
		SmtpEmailSenderAdapter adapter = new SmtpEmailSenderAdapter(mailSender, "no-reply@routia.test");

		adapter.send("user@example.com", "[Routia] 이메일 인증번호 안내", "<strong>123456</strong>");

		assertThat(message.getFrom()[0].toString()).isEqualTo("no-reply@routia.test");
		assertThat(message.getRecipients(Message.RecipientType.TO)[0].toString()).isEqualTo("user@example.com");
		assertThat(message.getSubject()).isEqualTo("[Routia] 이메일 인증번호 안내");
		assertThat(message.getContentType()).startsWith("text/html");
		assertThat(message.getContent().toString()).contains("123456");
	}
}
