package com.routiaback.auth.infrastructure;

import com.routiaback.auth.application.port.EmailSenderPort;
import com.routiaback.global.logging.SensitiveLogSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
class SmtpEmailSenderAdapter implements EmailSenderPort {

	private static final Logger log = LoggerFactory.getLogger(SmtpEmailSenderAdapter.class);

	private final JavaMailSender mailSender;
	private final String from;

	SmtpEmailSenderAdapter(JavaMailSender mailSender, @Value("${routia.mail.from}") String from) {
		this.mailSender = mailSender;
		this.from = from;
	}

	@Override
	public void send(String to, String subject, String html) {
		String maskedRecipient = SensitiveLogSanitizer.maskEmail(to);
		log.info("SMTP mail preparation started. recipient={} from={}", maskedRecipient, SensitiveLogSanitizer.maskEmail(from));
		try {
			log.debug("JavaMailSender.send invocation started. recipient={}", maskedRecipient);
			mailSender.send(message -> {
				MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
				helper.setFrom(from);
				helper.setTo(to);
				helper.setSubject(subject);
				helper.setText(html, true);
			});
			log.info("JavaMailSender.send completed. recipient={} status=success", maskedRecipient);
		} catch (RuntimeException exception) {
			Throwable rootCause = SensitiveLogSanitizer.rootCause(exception);
			log.error(
				"JavaMailSender.send failed. recipient={} exceptionClass={} message={} rootCauseClass={} rootCauseMessage={} stackTrace=\n{}",
				maskedRecipient,
				exception.getClass().getName(),
				SensitiveLogSanitizer.sanitize(exception.getMessage()),
				rootCause.getClass().getName(),
				SensitiveLogSanitizer.sanitize(rootCause.getMessage()),
				SensitiveLogSanitizer.stackTrace(exception)
			);
			throw exception;
		}
	}
}
